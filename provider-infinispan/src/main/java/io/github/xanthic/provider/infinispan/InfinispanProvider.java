package io.github.xanthic.provider.infinispan;

import io.github.xanthic.api.Cache;
import io.github.xanthic.api.ICacheSpec;
import io.github.xanthic.api.domain.ExpiryType;
import io.github.xanthic.core.AbstractCacheProvider;
import org.infinispan.commons.api.CacheContainerAdmin;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Provides {@link Cache} instances using Infinispan's {@link org.infinispan.Cache} in heap-mode.
 * <p>
 * Implements size and time-based expiry.
 */
public final class InfinispanProvider extends AbstractCacheProvider {
	@Override
	public <K, V> Cache<K, V> build(ICacheSpec<K, V> spec) {
		GlobalConfigurationBuilder global = GlobalConfigurationBuilder.defaultClusteredBuilder();
		DefaultCacheManager manager = new DefaultCacheManager(global.build());

		ConfigurationBuilder builder = new ConfigurationBuilder();
		if (spec.maxSize() != null) builder.memory().maxCount(spec.maxSize());
		handleExpiration(spec.expiryTime(), spec.expiryType(), (time, type) -> {
			if (type == ExpiryType.POST_WRITE)
				builder.expiration().lifespan(time.toNanos(), TimeUnit.NANOSECONDS);
			else
				builder.expiration().maxIdle(time.toNanos(), TimeUnit.NANOSECONDS);
		});

		org.infinispan.Cache<K, V> cache = manager.administration()
			.withFlags(CacheContainerAdmin.AdminFlag.VOLATILE)
			.getOrCreateCache(UUID.randomUUID().toString(), builder.build());

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
