package io.github.xanthic.cache.provider.infinispanjdk11;

import io.github.xanthic.cache.api.Cache;
import io.github.xanthic.cache.api.ICacheSpec;
import io.github.xanthic.cache.api.domain.ExpiryType;
import io.github.xanthic.cache.core.AbstractCacheProvider;
import org.infinispan.commons.api.CacheContainerAdmin;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Provides {@link Cache} instances using Infinispan's {@link org.infinispan.Cache} in heap-mode.
 * <p>
 * Implements size and time-based expiry.
 */
public final class InfinispanProvider extends AbstractCacheProvider {
	private static final EmbeddedCacheManager MANAGER = new DefaultCacheManager();

	@Override
	public <K, V> Cache<K, V> build(ICacheSpec<K, V> spec) {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.clustering().simpleCache(true);
		if (spec.maxSize() != null) builder.memory().maxCount(spec.maxSize());
		handleExpiration(spec.expiryTime(), spec.expiryType(), (time, type) -> {
			if (type == ExpiryType.POST_WRITE)
				builder.expiration().lifespan(time.toNanos(), TimeUnit.NANOSECONDS);
			else
				builder.expiration().maxIdle(time.toNanos(), TimeUnit.NANOSECONDS);
		});

		org.infinispan.Cache<K, V> cache = MANAGER.administration()
			.withFlags(CacheContainerAdmin.AdminFlag.VOLATILE)
			.createCache(UUID.randomUUID().toString(), builder.build());

		if (spec.removalListener() != null) {
			cache.addFilteredListener(
				new InfinispanListener<>(spec.removalListener()),
				(key, oldValue, oldMeta, newValue, newMeta, eventType) -> eventType != null && InfinispanListener.EVENTS.contains(eventType.getType()),
				null,
				InfinispanListener.ANNOTATIONS
			);
		}

		return new InfinispanDelegate<>(cache);
	}
}
