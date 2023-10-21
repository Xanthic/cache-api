package io.github.xanthic.jackson.util;

import io.github.xanthic.cache.api.Cache;
import io.github.xanthic.cache.api.CacheProvider;
import io.github.xanthic.cache.api.ICacheSpec;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
public class TrackedCacheProvider implements CacheProvider {
	CacheProvider underlyingProvider;
	List<TrackedCache<?, ?>> constructedCaches = new ArrayList<>();

	@Override
	public <K, V> Cache<K, V> build(ICacheSpec<K, V> spec) {
		TrackedCache<K, V> cache = new TrackedCache<>(underlyingProvider.build(spec));
		constructedCaches.add(cache);
		return cache;
	}
}
