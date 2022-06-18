package io.github.iprodigy.cache.providers;

import io.github.iprodigy.cache.Cache;
import io.github.iprodigy.cache.ExpiryType;
import io.github.iprodigy.cache.ICacheSpec;
import io.github.iprodigy.cache.RemovalCause;
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

import java.util.UUID;

public final class EhcacheProvider extends AbstractCacheProvider {
	@Override
	public <K, V> Cache<K, V> build(ICacheSpec<K, V> spec) {
		CacheManager manager = CacheManagerBuilder.newCacheManagerBuilder().build(true);

		//noinspection unchecked
		final CacheConfigurationBuilder<Object, Object>[] builder = new CacheConfigurationBuilder[] {
			CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, Object.class, poolBuilder(spec.maxSize()))
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
