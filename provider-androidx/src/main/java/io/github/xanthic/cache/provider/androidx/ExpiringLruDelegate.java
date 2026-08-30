package io.github.xanthic.cache.provider.androidx;

import androidx.collection.LruCache;
import io.github.xanthic.cache.api.RemovalListener;
import io.github.xanthic.cache.api.domain.ExpiryType;
import io.github.xanthic.cache.api.domain.RemovalCause;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

@Value
@Getter(AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false)
class ExpiringLruDelegate<K, V> extends AbstractCache<K, V> {
	@EqualsAndHashCode.Exclude
	Long maxSize;
	@EqualsAndHashCode.Exclude
	RemovalListener<K, V> listener;
	@EqualsAndHashCode.Exclude
	long expiry;
	@EqualsAndHashCode.Exclude
	ExpiryType type;
	@EqualsAndHashCode.Exclude
	ScheduledExecutorService exec;
	@EqualsAndHashCode.Exclude
	boolean shouldCloseExecutor;
	@EqualsAndHashCode.Exclude
	Map<Map.Entry<K, V>, Future<?>> tracker = new ConcurrentHashMap<>();

	LruCache<K, V> cache = new LruCache<K, V>(getMaxSize() != null ? getMaxSize().intValue() : Integer.MAX_VALUE) {
		@Override
		protected void entryRemoved(boolean evicted, @NotNull K key, @NotNull V oldValue, @Nullable V newValue) {
			RemovalCause cause;
			if (evicted) {
				cause = RemovalCause.SIZE;
			} else if (newValue != null) {
				cause = RemovalCause.REPLACED;
			} else {
				Future<?> fut = tracker.get(new AbstractMap.SimpleEntry<>(key, oldValue));
				if (fut != null && !fut.isCancelled()) {
					cause = RemovalCause.TIME;
				} else {
					cause = RemovalCause.MANUAL;
				}
			}

			if (listener != null)
				listener.onRemoval(key, oldValue, cause);
		}
	};

	@Override
	public V get(@NotNull K key) {
		synchronized (getLock()) {
			V v = cache.get(key);
			if (type == ExpiryType.POST_ACCESS) start(key, v);
			return v;
		}
	}

	@Override
	public V put(@NotNull K key, @NotNull V value) {
		synchronized (getLock()) {
			V prev = cache.put(key, value);
			start(key, value);
			if (prev != value) cancelIfRunning(key, prev); // mapping was already removed
			if (maxSize != null) cache.trimToSize(maxSize.intValue());
			return prev;
		}
	}

	@Override
	public V remove(@NotNull K key) {
		synchronized (getLock()) {
			cancelIfRunning(key, cache.get(key)); // mapping is being removed already
			return cache.remove(key);
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
	public void close() {
		super.close();
		if (shouldCloseExecutor) {
			this.exec.shutdownNow();
		}
	}

	@Override
	public long size() {
		return cache.size();
	}

	@Override
	public void forEach(@NotNull BiConsumer<? super K, ? super V> action) {
		if (type == ExpiryType.POST_ACCESS) {
			synchronized (getLock()) {
				BiConsumer<K, V> markAccessed = this::start;
				cache.snapshot().forEach(markAccessed.andThen(action));
			}
		} else {
			cache.snapshot().forEach(action);
		}
	}

	@NotNull
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
				if (!Thread.interrupted() && value == cache.get(key))
					cache.remove(key);
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
