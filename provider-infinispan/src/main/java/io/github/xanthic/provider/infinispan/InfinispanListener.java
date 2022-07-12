package io.github.xanthic.provider.infinispan;

import io.github.xanthic.api.RemovalListener;
import io.github.xanthic.api.domain.RemovalCause;
import lombok.Value;
import org.infinispan.notifications.Listener;
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

@Value
@Listener
class InfinispanListener<K, V> {
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
