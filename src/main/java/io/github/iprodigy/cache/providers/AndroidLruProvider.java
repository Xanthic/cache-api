package io.github.iprodigy.cache.providers;

import androidx.collection.LruCache;
import io.github.iprodigy.cache.Cache;
import io.github.iprodigy.cache.CacheApiSettings;
import io.github.iprodigy.cache.ExpiryType;
import io.github.iprodigy.cache.MisconfigurationPolicy;
import io.github.iprodigy.cache.MisconfiguredCacheException;
import io.github.iprodigy.cache.RemovalCause;
import io.github.iprodigy.cache.RemovalListener;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

public class AndroidLruProvider implements CacheProvider {
	@Override
	public <K, V> Cache<K, V> build(
		@Nullable Long maxSize,
		@Nullable Duration expiryTime,
		@Nullable ExpiryType expiryType,
		@Nullable RemovalListener<K, V> removalListener,
		@Nullable ScheduledExecutorService executor
	) {
		MisconfigurationPolicy configPolicy = CacheApiSettings.getInstance().getDefaultMisconfigurationPolicy();

		if (expiryTime != null && configPolicy == MisconfigurationPolicy.REJECT)
			throw new MisconfiguredCacheException("Expiration is not natively supported in Android's LRU Cache");

		int size = maxSize != null ? maxSize.intValue() : Integer.MAX_VALUE;
		LruCache<K, V> cache = new LruCache<K, V>(size) {
			@Override
			protected void entryRemoved(boolean evicted, @NotNull K key, @NotNull V oldValue, @Nullable V newValue) {
				RemovalCause cause;
				if (evicted) {
					cause = RemovalCause.SIZE;
				} else if (newValue != null) {
					cause = RemovalCause.REPLACED;
				} else {
					cause = RemovalCause.MANUAL;
				}

				if (removalListener != null)
					removalListener.onRemoval(key, oldValue, cause);
			}
		};

		return new LruDelegate<>(cache);
	}

	@Value
	@EqualsAndHashCode(callSuper = false)
	private static class LruDelegate<K, V> extends AbstractCache<K, V> {
		LruCache<K, V> cache;

		@Override
		public V get(K key) {
			return cache.get(key);
		}

		@Override
		public V put(K key, V value) {
			return cache.put(key, value);
		}

		@Override
		public V remove(K key) {
			return cache.remove(key);
		}

		@Override
		public void clear() {
			cache.evictAll();
		}

		@Override
		public long size() {
			return cache.size();
		}

		@Override
		protected Object getLock() {
			return this.cache;
		}
	}
}
