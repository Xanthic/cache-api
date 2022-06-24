package io.github.iprodigy.cache.provider.androidx;

import androidx.collection.LruCache;
import io.github.iprodigy.cache.api.Cache;
import io.github.iprodigy.cache.api.ICacheSpec;
import io.github.iprodigy.cache.api.RemovalListener;
import io.github.iprodigy.cache.api.domain.RemovalCause;
import io.github.iprodigy.cache.core.AbstractCache;
import io.github.iprodigy.cache.core.AbstractCacheProvider;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AndroidLruProvider extends AbstractCacheProvider {
	@Override
	public <K, V> Cache<K, V> build(ICacheSpec<K, V> spec) {
		handleUnsupportedExpiry(spec.expiryTime());
		return new LruDelegate<>(build(spec.maxSize(), spec.removalListener()));
	}

	static <K, V> LruCache<K, V> build(Long maxSize, RemovalListener<K, V> listener) {
		int size = maxSize != null ? maxSize.intValue() : Integer.MAX_VALUE;
		return new LruCache<K, V>(size) {
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

				if (listener != null)
					listener.onRemoval(key, oldValue, cause);
			}
		};
	}

	@Value
	@EqualsAndHashCode(callSuper = false)
	static class LruDelegate<K, V> extends AbstractCache<K, V> {
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
