package io.github.iprodigy.cache.core;

import io.github.iprodigy.cache.api.Cache;
import io.github.iprodigy.cache.api.exception.MisconfiguredCacheException;
import io.github.iprodigy.cache.api.exception.NoDefaultCacheImplementationException;

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
	 * @see io.github.iprodigy.cache.api.ICacheSpec
	 * @see CacheApiSettings
	 */
	public static <K, V> Cache<K, V> create(Consumer<CacheApiSpec<K, V>> spec) {
		CacheApiSpec<K, V> finalSpec = CacheApiSpec.process(spec);
		return finalSpec.provider().build(finalSpec);
	}

}
