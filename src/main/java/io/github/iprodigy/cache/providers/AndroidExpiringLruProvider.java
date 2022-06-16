package io.github.iprodigy.cache.providers;

import androidx.collection.LruCache;
import io.github.iprodigy.cache.Cache;
import io.github.iprodigy.cache.ExpiryType;
import io.github.iprodigy.cache.RemovalListener;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class AndroidExpiringLruProvider extends AbstractCacheProvider {
	@Override
	public <K, V> Cache<K, V> build(
		@Nullable Long maxSize,
		@Nullable Duration expiryTime,
		@Nullable ExpiryType expiryType,
		@Nullable RemovalListener<K, V> removalListener,
		@Nullable ScheduledExecutorService executor
	) {
		if (executor == null) handleUnsupportedExpiry(expiryTime);
		LruCache<K, V> cache = AndroidLruProvider.build(maxSize, removalListener);
		if (expiryTime == null) return new AndroidLruProvider.LruDelegate<>(cache);
		return new ExpiringLruDelegate<>(cache, expiryTime.toNanos(), getExpiryType(expiryType), executor);
	}

	@Value
	@Getter(AccessLevel.PRIVATE)
	@EqualsAndHashCode(callSuper = false)
	private static class ExpiringLruDelegate<K, V> extends AbstractCache<K, V> {
		LruCache<K, V> cache;
		@EqualsAndHashCode.Exclude
		long expiry;
		@EqualsAndHashCode.Exclude
		ExpiryType type;
		@EqualsAndHashCode.Exclude
		ScheduledExecutorService exec;
		@EqualsAndHashCode.Exclude
		Map<Map.Entry<K, V>, Future<?>> tracker = new ConcurrentHashMap<>();

		@Override
		public V get(K key) {
			synchronized (getLock()) {
				V v = cache.get(key);
				if (type == ExpiryType.POST_ACCESS) start(key, v);
				return v;
			}
		}

		@Override
		public V put(K key, V value) {
			synchronized (getLock()) {
				V prev = cache.put(key, value);
				start(key, value);
				if (prev != value) cancelIfRunning(key, prev); // mapping was already removed
				return prev;
			}
		}

		@Override
		public V remove(K key) {
			synchronized (getLock()) {
				V removed = cache.remove(key);
				cancelIfRunning(key, removed); // mapping was already removed
				return removed;
			}
		}

		@Override
		public void clear() {
			synchronized (getLock()) {
				cache.evictAll();
				tracker.values().forEach(fut -> fut.cancel(true));
				tracker.clear();
			}
		}

		@Override
		public long size() {
			return cache.size();
		}

		@Override
		protected Object getLock() {
			return this.cache;
		}

		private void start(final K key, final V value) {
			if (value == null) return;

			final AbstractMap.SimpleEntry<K, V> entry = new AbstractMap.SimpleEntry<>(key, value);

			final AtomicReference<Future<?>> futRef = new AtomicReference<>();
			final Future<?> future = exec.schedule(() -> {
				synchronized (getLock()) {
					if (!Thread.interrupted()) {
						V removed = cache.remove(key);
						if (removed != null && removed != value)
							cache.put(key, removed);
					}
				}
				tracker.remove(entry, futRef.get());
				futRef.lazySet(null);
			}, expiry, TimeUnit.NANOSECONDS);
			futRef.set(future);

			tracker.merge(
				entry,
				future,
				(oldFuture, newFuture) -> {
					if (oldFuture.isDone() && cache.get(key) == null && !newFuture.isDone()) cache.put(key, value);
					else oldFuture.cancel(true);
					return newFuture;
				}
			);
		}

		private void cancelIfRunning(final K key, final V value) {
			if (value == null) return;
			final AbstractMap.SimpleEntry<K, V> entry = new AbstractMap.SimpleEntry<>(key, value);
			final Future<?> future = tracker.remove(entry);
			if (future != null)
				future.cancel(true);
		}
	}
}
