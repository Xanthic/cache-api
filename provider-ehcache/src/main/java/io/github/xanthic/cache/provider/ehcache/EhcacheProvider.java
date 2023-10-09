package io.github.xanthic.cache.provider.ehcache;

import io.github.xanthic.cache.api.Cache;
import io.github.xanthic.cache.api.ICacheSpec;
import io.github.xanthic.cache.api.domain.ExpiryType;
import io.github.xanthic.cache.api.domain.RemovalCause;
import io.github.xanthic.cache.core.AbstractCacheProvider;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.core.spi.time.TickingTimeSource;
import org.ehcache.event.EventType;
import org.ehcache.impl.internal.TimeSourceConfiguration;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Provides {@link Cache} instances using {@link org.ehcache.core.Ehcache} in heap-mode.
 * <p>
 * Implements size and time-based expiry.
 * <p>
 * Specifying {@link ICacheSpec#maxSize()} is highly recommended.
 */
public final class EhcacheProvider extends AbstractCacheProvider {

	@Override
	public <K, V> Cache<K, V> build(ICacheSpec<K, V> spec) {
		CacheManagerBuilder<CacheManager> managerBuilder = CacheManagerBuilder.newCacheManagerBuilder();
		if (Boolean.TRUE.equals(spec.highContention()) && spec.expiryTime() != null) {
			// https://www.ehcache.org/documentation/3.10/performance.html#time-source
			managerBuilder = managerBuilder.using(
				new TimeSourceConfiguration(new TickingTimeSource(1L, 1000L))
			);
		}
		CacheManager manager = managerBuilder.build(true);

		//noinspection unchecked
		final CacheConfigurationBuilder<Object, Object>[] builder = new CacheConfigurationBuilder[] {
			CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, Object.class,
				ResourcePoolsBuilder.heap(spec.maxSize() != null ? spec.maxSize() : Long.MAX_VALUE)
			)
		};

		handleExpiration(spec.expiryTime(), spec.expiryType(), (time, type) -> {
			if (type == ExpiryType.POST_WRITE)
				builder[0] = builder[0].withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(time));
			else
				builder[0] = builder[0].withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(time));
		});

		if (spec.removalListener() != null) {
			builder[0] = builder[0].withService(
				CacheEventListenerConfigurationBuilder.newEventListenerConfiguration(
					e -> {
						//noinspection unchecked
						spec.removalListener().onRemoval((K) e.getKey(), (V) e.getOldValue(), getCause(e.getType()));
					},
					EventType.EVICTED, EventType.EXPIRED, EventType.REMOVED, EventType.UPDATED
				)
			);
		}

		org.ehcache.Cache<Object, Object> cache = manager.createCache(UUID.randomUUID().toString(), builder[0]);
		EhcacheDelegate<K, V> delegate = new EhcacheDelegate<>(cache);

		// background thread for faster eviction events since ehcache does not offer prompt expiration
		if (spec.removalListener() != null && spec.executor() != null && spec.expiryTime() != null && !spec.expiryTime().isZero()) {
			spec.executor().scheduleAtFixedRate(
				delegate::size,
				spec.expiryTime().toNanos(),
				Math.min(spec.expiryTime().toNanos(), Duration.ofMinutes(1L).toNanos()),
				TimeUnit.NANOSECONDS
			);
		}

		return delegate;
	}

	private static RemovalCause getCause(EventType type) {
		switch (type) {
			case EVICTED:
				return RemovalCause.SIZE;
			case EXPIRED:
				return RemovalCause.TIME;
			case REMOVED:
				return RemovalCause.MANUAL;
			case UPDATED:
				return RemovalCause.REPLACED;
			default:
				return RemovalCause.OTHER;
		}
	}

}
