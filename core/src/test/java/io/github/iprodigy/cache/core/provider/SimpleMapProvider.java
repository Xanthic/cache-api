package io.github.iprodigy.cache.core.provider;

import io.github.iprodigy.cache.api.Cache;
import io.github.iprodigy.cache.api.ICacheSpec;
import io.github.iprodigy.cache.core.AbstractCacheProvider;
import io.github.iprodigy.cache.core.delegate.GenericMapCacheDelegate;

import java.util.HashMap;

/**
 * A very simple map cache provider for testing purposes
 */
public final class SimpleMapProvider extends AbstractCacheProvider {

	@Override
	public <K, V> Cache<K, V> build(ICacheSpec<K, V> spec) {
		return new GenericMapCacheDelegate<>(new HashMap<>());
	}

}
