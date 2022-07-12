package io.github.xanthic.provider.expiringmap;

import io.github.xanthic.api.Cache;
import io.github.xanthic.api.ICacheSpec;
import io.github.xanthic.api.domain.ExpiryType;
import io.github.xanthic.api.domain.RemovalCause;
import io.github.xanthic.core.AbstractCacheProvider;
import io.github.xanthic.core.delegate.GenericMapCacheDelegate;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

import java.util.concurrent.TimeUnit;

/**
 * Provides {@link Cache} instances using {@link ExpiringMap}.
 * <p>
 * Implements size and time-based expiry.
 * <p>
 * Note: listeners will always receive {@link RemovalCause#OTHER} due to limitations of the backing library.
 * <p>
 * Consider using Caffeine or Cache2k for better performance.
 */
public final class ExpiringMapProvider extends AbstractCacheProvider {
	@Override
	public <K, V> Cache<K, V> build(ICacheSpec<K, V> spec) {
		ExpiringMap.Builder<Object, Object> builder = ExpiringMap.builder();
		if (spec.maxSize() != null) builder.maxSize(spec.maxSize().intValue());
		if (spec.removalListener() != null) builder.<K, V>expirationListener((key, value) -> spec.removalListener().onRemoval(key, value, RemovalCause.OTHER));
		handleExpiration(spec.expiryTime(), spec.expiryType(), (time, type) -> {
			builder.expiration(time.toNanos(), TimeUnit.NANOSECONDS);
			if (type == ExpiryType.POST_WRITE)
				builder.expirationPolicy(ExpirationPolicy.CREATED);
			else
				builder.expirationPolicy(ExpirationPolicy.ACCESSED);
		});

		return new GenericMapCacheDelegate<>(builder.build());
	}
}
