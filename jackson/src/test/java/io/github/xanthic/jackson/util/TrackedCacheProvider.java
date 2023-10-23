package io.github.xanthic.jackson.util;

import io.github.xanthic.cache.api.Cache;
import io.github.xanthic.cache.api.CacheProvider;
import io.github.xanthic.cache.api.ICacheSpec;
import io.github.xanthic.cache.core.CacheApiSettings;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
public class TrackedCacheProvider implements CacheProvider {
	CacheProvider underlyingProvider = CacheApiSettings.getInstance().getDefaultCacheProvider();
	List<Cache<?, ?>> constructedCaches = new ArrayList<>();

	@Override
	public <K, V> Cache<K, V> build(ICacheSpec<K, V> spec) {
		Cache<K, V> cache = underlyingProvider.build(spec);
		constructedCaches.add(cache);
		return cache;
	}
}
