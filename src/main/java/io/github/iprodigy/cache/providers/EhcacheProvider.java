package io.github.iprodigy.cache.providers;

import io.github.iprodigy.cache.Cache;
import io.github.iprodigy.cache.ExpiryType;
import io.github.iprodigy.cache.RemovalCause;
import io.github.iprodigy.cache.RemovalListener;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.event.EventType;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;

public final class EhcacheProvider extends AbstractCacheProvider {
	@Override
	public <K, V> Cache<K, V> build(
		@Nullable Long maxSize,
		@Nullable Duration expiryTime,
		@Nullable ExpiryType expiryType,
		@Nullable RemovalListener<K, V> removalListener,
		@Nullable ScheduledExecutorService executor
	) {
		CacheManager manager = CacheManagerBuilder.newCacheManagerBuilder().build(true);

		//noinspection unchecked
		final CacheConfigurationBuilder<Object, Object>[] builder = new CacheConfigurationBuilder[] {
			CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, Object.class, poolBuilder(maxSize))
		};

		handleExpiration(expiryTime, expiryType, (time, type) -> {
			if (type == ExpiryType.POST_WRITE)
				builder[0] = builder[0].withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(time));
			else
				builder[0] = builder[0].withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(time));
		});

		if (removalListener != null) {
			builder[0] = builder[0].withService(
				CacheEventListenerConfigurationBuilder.newEventListenerConfiguration(
					e -> {
						//noinspection unchecked
						removalListener.onRemoval((K) e.getKey(), (V) e.getOldValue(), getCause(e.getType()));
					},
					EventType.EVICTED, EventType.EXPIRED, EventType.REMOVED, EventType.UPDATED
				)
			);
		}

		org.ehcache.Cache<Object, Object> cache = manager.createCache(UUID.randomUUID().toString(), builder[0]);
		return new EhcacheDelegate<>(cache);
	}

	private static ResourcePoolsBuilder poolBuilder(Long maxSize) {
		if (maxSize == null)
			return ResourcePoolsBuilder.newResourcePoolsBuilder().heap(Runtime.getRuntime().maxMemory() / 2, MemoryUnit.B);
		return ResourcePoolsBuilder.heap(maxSize);
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

	@Value
	@EqualsAndHashCode(callSuper = false)
	@SuppressWarnings("unchecked")
	private static class EhcacheDelegate<K, V> extends AbstractCache<K, V> {
		org.ehcache.Cache<Object, Object> cache;

		@Override
		public V get(K key) {
			return (V) cache.get(key);
		}

		@Override
		public V put(K key, V value) {
			synchronized (getLock()) {
				V old = this.get(key);
				cache.put(key, value);
				return old;
			}
		}

		@Override
		public V remove(K key) {
			synchronized (getLock()) {
				V old = this.get(key);
				cache.remove(key);
				return old;
			}
		}

		@Override
		public void clear() {
			cache.clear();
		}

		@Override
		public long size() {
			long n = 0;
			for (org.ehcache.Cache.Entry<Object, Object> ignored : cache) {
				n++;
			}
			return n;
		}

		@Override
		public V putIfAbsent(K key, V value) {
			return (V) cache.putIfAbsent(key, value);
		}

		@Override
		protected Object getLock() {
			return this.cache;
		}
	}
}
