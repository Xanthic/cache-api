package io.github.xanthic.cache.provider.androidx;

import androidx.collection.LruCache;
import io.github.xanthic.cache.api.Cache;
import io.github.xanthic.cache.api.ICacheSpec;
import io.github.xanthic.cache.api.RemovalListener;
import io.github.xanthic.cache.core.AbstractCacheProvider;
import io.github.xanthic.cache.api.domain.RemovalCause;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Provides {@link Cache} instances using {@link LruCache}.
 * <p>
 * Supports size and time-based eviction.
 * <p>
 * Note: on {@link Cache#clear()}, listeners will receive {@link RemovalCause#SIZE}
 * due to backing library limitations.
 * <p>
 * Consider using Cache2k for better performance.
 */
public final class AndroidLruProvider extends AbstractCacheProvider {

	@Override
	public <K, V> Cache<K, V> build(ICacheSpec<K, V> spec) {
		ScheduledExecutorService executor = spec.executor();
		Duration expiryTime = spec.expiryTime();
		if (executor == null) handleUnsupportedExpiry(expiryTime);
		if (expiryTime == null) return new LruDelegate<>(buildSimple(spec.maxSize(), spec.removalListener()));
		ScheduledExecutorService exec;
		boolean createdExecutor;
        if (executor != null) {
            exec = executor;
			createdExecutor = false;
        } else {
            exec = Executors.newSingleThreadScheduledExecutor();
			createdExecutor = true;
        }
        return new ExpiringLruDelegate<>(spec.maxSize(), spec.removalListener(), expiryTime.toNanos(), getExpiryType(spec.expiryType()), exec, createdExecutor);
	}

	private static <K, V> LruCache<K, V> buildSimple(Long maxSize, RemovalListener<K, V> listener) {
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
