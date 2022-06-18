package io.github.iprodigy.cache.providers;

import io.github.iprodigy.cache.Cache;
import io.github.iprodigy.cache.ICacheSpec;

@FunctionalInterface
public interface CacheProvider {
	<K, V> Cache<K, V> build(ICacheSpec<K, V> spec);
}
