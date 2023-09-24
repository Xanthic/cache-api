package io.github.xanthic.cache.core;

import io.github.xanthic.cache.api.Cache;
import io.github.xanthic.cache.api.ICacheSpec;
import io.github.xanthic.cache.api.exception.MisconfiguredCacheException;
import io.github.xanthic.cache.api.exception.NoDefaultCacheImplementationException;
import io.github.xanthic.cache.core.delegate.EmptyCache;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * Primary API interaction point to build {@link Cache} instances.
 *
 * @see #create(Consumer)
 */
public final class CacheApi {

	private CacheApi() {
		// prevent direct instantiation
	}

	/**
	 * Builds a generic {@link Cache} according to the desired specification.
	 *
	 * @param spec consumer in which the desired cache settings should be specified
	 * @param <K>  the type of keys that form the cache
	 * @param <V>  the type of values that are contained in the cache
	 * @return {@link Cache}
	 * @throws NoDefaultCacheImplementationException if a provider is not specified and no default provider has been set
	 * @throws MisconfiguredCacheException           if the cache settings are invalid (e.g., negative max size or expiry time)
	 * @see ICacheSpec
	 * @see CacheApiSettings
	 */
	public static <K, V> Cache<K, V> create(Consumer<CacheApiSpec<K, V>> spec) {
		CacheApiSpec<K, V> finalSpec = CacheApiSpec.process(spec);
		if (isPermanentlyEmpty(finalSpec)) return EmptyCache.get();
		return finalSpec.provider().build(finalSpec);
	}

	private static boolean isPermanentlyEmpty(ICacheSpec<?, ?> spec) {
		Long maxSize = spec.maxSize();
		Duration expiryTime = spec.expiryTime();
		return (maxSize != null && maxSize == 0) || (expiryTime != null && expiryTime.isZero());
	}

}
