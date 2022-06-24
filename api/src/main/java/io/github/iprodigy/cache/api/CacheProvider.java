package io.github.iprodigy.cache.api;

@FunctionalInterface
public interface CacheProvider {
	<K, V> Cache<K, V> build(ICacheSpec<K, V> spec);
}
