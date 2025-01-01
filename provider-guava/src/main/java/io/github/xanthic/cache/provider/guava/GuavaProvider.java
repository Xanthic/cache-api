package io.github.xanthic.cache.provider.guava;

import com.google.common.cache.CacheBuilder;
import io.github.xanthic.cache.api.Cache;
import io.github.xanthic.cache.api.ICacheSpec;
import io.github.xanthic.cache.api.domain.ExpiryType;
import io.github.xanthic.cache.api.domain.RemovalCause;
import io.github.xanthic.cache.core.AbstractCacheProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Provides {@link Cache} instances using Guava's {@link CacheBuilder}.
 * <p>
 * Implements size and time-based expiry.
 * <p>
 * Consider using Caffeine over Guava, if not targeting Android, for better performance.
 */
public final class GuavaProvider extends AbstractCacheProvider {
	@Override
	public <K, V> Cache<K, V> build(ICacheSpec<K, V> spec) {
		CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
		if (spec.maxSize() != null) builder.maximumSize(spec.maxSize());
		if (Boolean.TRUE.equals(spec.highContention())) {
			// https://github.com/google/guava/issues/2063
			builder.concurrencyLevel(64);
		}
		if (spec.removalListener() != null) {
			//noinspection ConstantConditions
			builder = builder.removalListener(e -> {
				//noinspection unchecked
				spec.removalListener().onRemoval((K) e.getKey(), (V) e.getValue(), getCause(e.getCause()));
			});
		}
		CacheBuilder<Object, Object> finalBuilder = builder;
		handleExpiration(spec.expiryTime(), spec.expiryType(), (time, type) -> {
			if (type == ExpiryType.POST_WRITE)
				finalBuilder.expireAfterWrite(time);
			else
				finalBuilder.expireAfterAccess(time);
		});

		com.google.common.cache.Cache<K, V> cache = finalBuilder.build();

		// background thread for faster eviction events since guava does not offer prompt expiration
		if (spec.removalListener() != null && spec.executor() != null && spec.expiryTime() != null && !spec.expiryTime().isZero()) {
			spec.executor().scheduleAtFixedRate(
				cache::cleanUp,
				spec.expiryTime().toNanos(),
				Math.min(spec.expiryTime().toNanos(), Duration.ofMinutes(1L).toNanos()),
				TimeUnit.NANOSECONDS
			);
		}

		return new GuavaDelegate<>(cache);
	}

	@SuppressWarnings("DuplicatedCode")
	private static RemovalCause getCause(com.google.common.cache.RemovalCause cause) {
		switch (cause) {
			case EXPLICIT:
				return RemovalCause.MANUAL;
			case REPLACED:
				return RemovalCause.REPLACED;
			case EXPIRED:
				return RemovalCause.TIME;
			case SIZE:
				return RemovalCause.SIZE;
			case COLLECTED:
			default:
				return RemovalCause.OTHER;
		}
	}

	@Override
	public int getDiscoveryOrder() {
		return 9;
	}
}
