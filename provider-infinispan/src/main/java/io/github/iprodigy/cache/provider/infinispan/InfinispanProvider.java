package io.github.iprodigy.cache.provider.infinispan;

import io.github.iprodigy.cache.api.Cache;
import io.github.iprodigy.cache.api.ICacheSpec;
import io.github.iprodigy.cache.api.RemovalListener;
import io.github.iprodigy.cache.api.domain.ExpiryType;
import io.github.iprodigy.cache.api.domain.RemovalCause;
import io.github.iprodigy.cache.core.AbstractCacheProvider;
import lombok.Value;
import org.infinispan.commons.api.CacheContainerAdmin;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.notifications.cachelistener.annotation.CacheEntriesEvicted;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryExpired;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryInvalidated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.event.CacheEntriesEvictedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryExpiredEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryInvalidatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;
import org.infinispan.notifications.cachelistener.event.Event;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

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

	@Value
	private static class InfinispanListener<K, V> {
		static final Set<Event.Type> EVENTS;
		static final Set<Class<? extends Annotation>> ANNOTATIONS;

		RemovalListener<K, V> removalListener;

		@CacheEntriesEvicted
		public void onPostEvictions(CacheEntriesEvictedEvent<K, V> event) {
			if (!event.isPre())
				event.getEntries().forEach((k, v) -> removalListener.onRemoval(k, v, RemovalCause.SIZE));
		}

		@CacheEntryExpired
		public void onExpiry(CacheEntryExpiredEvent<K, V> event) {
			removalListener.onRemoval(event.getKey(), event.getValue(), RemovalCause.TIME);
		}

		@CacheEntryInvalidated
		public void onInvalidation(CacheEntryInvalidatedEvent<K, V> event) {
			removalListener.onRemoval(event.getKey(), event.getValue(), RemovalCause.OTHER);
		}

		@CacheEntryModified
		public void onPostModifyExisting(CacheEntryModifiedEvent<K, V> event) {
			if (!event.isCreated() && !event.isPre())
				removalListener.onRemoval(event.getKey(), event.getOldValue(), RemovalCause.REPLACED);
		}

		@CacheEntryRemoved
		public void onPostRemoval(CacheEntryRemovedEvent<K, V> event) {
			if (!event.isPre())
				removalListener.onRemoval(event.getKey(), event.getOldValue(), RemovalCause.MANUAL);
		}

		static {
			EVENTS = EnumSet.noneOf(Event.Type.class);
			EVENTS.add(Event.Type.CACHE_ENTRY_EVICTED);
			EVENTS.add(Event.Type.CACHE_ENTRY_EXPIRED);
			EVENTS.add(Event.Type.CACHE_ENTRY_INVALIDATED);
			EVENTS.add(Event.Type.CACHE_ENTRY_MODIFIED);
			EVENTS.add(Event.Type.CACHE_ENTRY_REMOVED);

			ANNOTATIONS = Collections.newSetFromMap(new IdentityHashMap<>());
			ANNOTATIONS.add(CacheEntriesEvicted.class);
			ANNOTATIONS.add(CacheEntryExpired.class);
			ANNOTATIONS.add(CacheEntryInvalidated.class);
			ANNOTATIONS.add(CacheEntryModified.class);
			ANNOTATIONS.add(CacheEntryRemoved.class);
		}
	}

	@Value
	private static class InfinispanDelegate<K, V> implements Cache<K, V> {
		org.infinispan.Cache<K, V> cache;

		@Override
		public V get(K key) {
			return cache.get(key);
		}

		@Override
		public V put(K key, V value) {
			return cache.put(key, value);
		}

		@Override
		public V remove(K key) {
			return cache.remove(key);
		}

		@Override
		public void clear() {
			cache.clear();
		}

		@Override
		public long size() {
			return cache.size();
		}

		@Override
		public V computeIfAbsent(K key, Function<K, V> computeFunc) {
			return cache.computeIfAbsent(key, computeFunc);
		}

		@Override
		public V putIfAbsent(K key, V value) {
			return cache.putIfAbsent(key, value);
		}

		@Override
		public V merge(K key, V value, BiFunction<V, V, V> mergeFunc) {
			return cache.merge(key, value, mergeFunc);
		}
	}
}
