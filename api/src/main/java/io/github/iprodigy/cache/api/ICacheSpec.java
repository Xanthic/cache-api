package io.github.iprodigy.cache.api;

import io.github.iprodigy.cache.api.domain.ExpiryType;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

public interface ICacheSpec<K, V> {

	CacheProvider provider();

	Long maxSize();

	Duration expiryTime();

	ExpiryType expiryType();

	RemovalListener<K, V> removalListener();

	ScheduledExecutorService executor();

}
