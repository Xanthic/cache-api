package io.github.xanthic.api;

/**
 * Creates {@link Cache} instances using some backing implementation
 */
@FunctionalInterface
public interface CacheProvider {

	/**
	 * Builds a new {@link Cache} instance according to the supplied specification
	 *
	 * @param spec The cache specification that the new instance should be configured to satisfy
	 * @param <K>  The type of the keys that form the cache
	 * @param <V>  The type of the values that are contained in the cache
	 * @return a new {@link Cache} instance that conforms with the supplied specification
	 */
	<K, V> Cache<K, V> build(ICacheSpec<K, V> spec);

}
