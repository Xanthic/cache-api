package io.github.iprodigy.cache.provider.androidx;

import androidx.collection.LruCache;
import io.github.iprodigy.cache.api.Cache;
import io.github.iprodigy.cache.api.ICacheSpec;
import io.github.iprodigy.cache.api.RemovalListener;
import io.github.iprodigy.cache.api.domain.ExpiryType;
import io.github.iprodigy.cache.api.domain.RemovalCause;
import io.github.iprodigy.cache.core.AbstractCacheProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AndroidLruProvider extends AbstractCacheProvider {

	@Override
	public <K, V> Cache<K, V> build(ICacheSpec<K, V> spec) {
		handleUnsupportedExpiry(spec.expiryTime());
		return new LruDelegate<>(build(spec.maxSize(), spec.removalListener()));
	}

	@Override
	protected ExpiryType preferredType() {
		return null; // expiry is not supported by this provider
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

}
