package io.github.iprodigy.cache;

import io.github.iprodigy.cache.providers.CacheProvider;
import lombok.Builder;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

public final class CacheApi {

	private CacheApi() {
		// prevent direct instantiation
	}

	@Builder
	private static <K, V> Cache<K, V> create(
		CacheProvider provider,
		Long maxSize,
		Duration expiryTime,
		ExpiryType expiryType,
		RemovalListener<K, V> removalListener,
		ScheduledExecutorService executor
	) {
		CacheProvider cacheProvider = provider != null ? provider : CacheApiSettings.getInstance().getDefaultCacheProvider();
		return cacheProvider.build(maxSize, expiryTime, expiryType, removalListener, executor);
	}

}
