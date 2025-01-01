package io.github.xanthic.cache.provider.caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;
import io.github.xanthic.cache.api.Cache;
import io.github.xanthic.cache.api.ICacheSpec;
import io.github.xanthic.cache.api.domain.ExpiryType;
import io.github.xanthic.cache.api.domain.RemovalCause;
import io.github.xanthic.cache.core.AbstractCacheProvider;

/**
 * Provides {@link Cache} instances using {@link Caffeine}.
 * <p>
 * Implements size and time-based eviction.
 * <p>
 * For timely {@link ICacheSpec#removalListener()} calls, {@link ICacheSpec#executor()} should be specified.
 * <p>
 * This module depends on Caffeine version 2.x for Java 8 compatibility.
 */
public final class CaffeineProvider extends AbstractCacheProvider {

	@Override
	public <K, V> Cache<K, V> build(ICacheSpec<K, V> spec) {
		Caffeine<Object, Object> builder = Caffeine.newBuilder();
		if (spec.maxSize() != null) builder.maximumSize(spec.maxSize());
		if (spec.executor() != null) builder.scheduler(Scheduler.forScheduledExecutorService(spec.executor()));
		if (spec.removalListener() != null) builder.<K, V>removalListener((key, value, cause) -> spec.removalListener().onRemoval(key, value, getCause(cause)));
		handleExpiration(spec.expiryTime(), spec.expiryType(), (time, type) -> {
			if (type == ExpiryType.POST_WRITE)
				builder.expireAfterWrite(time);
			else
				builder.expireAfterAccess(time);
		});

		return new CaffeineDelegate<>(builder.build());
	}

	@SuppressWarnings("DuplicatedCode")
	private static RemovalCause getCause(com.github.benmanes.caffeine.cache.RemovalCause cause) {
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
		return 3;
	}
}
