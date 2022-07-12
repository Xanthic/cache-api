package io.github.xanthic.provider.androidx;

import androidx.collection.LruCache;
import io.github.xanthic.api.Cache;
import io.github.xanthic.api.ICacheSpec;
import io.github.xanthic.api.RemovalListener;
import io.github.xanthic.api.domain.ExpiryType;
import io.github.xanthic.api.domain.RemovalCause;
import io.github.xanthic.core.AbstractCacheProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides {@link Cache} instances using {@link LruCache}.
 * <p>
 * Supports size-based eviction.
 * However, time-based expiry is <i>not</i> supported, so {@link AndroidExpiringLruProvider} should be preferred.
 * <p>
 * Note: on {@link Cache#clear()}, listeners will receive {@link RemovalCause#SIZE}
 * due to backing library limitations.
 */
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
