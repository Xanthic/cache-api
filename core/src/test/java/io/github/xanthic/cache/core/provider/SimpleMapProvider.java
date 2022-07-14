package io.github.xanthic.cache.core.provider;

import io.github.xanthic.cache.api.Cache;
import io.github.xanthic.cache.api.ICacheSpec;
import io.github.xanthic.cache.core.AbstractCacheProvider;
import io.github.xanthic.cache.core.delegate.GenericMapCacheDelegate;

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
