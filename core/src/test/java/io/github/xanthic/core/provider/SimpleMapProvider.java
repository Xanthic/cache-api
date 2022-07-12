package io.github.xanthic.core.provider;

import io.github.xanthic.api.Cache;
import io.github.xanthic.api.ICacheSpec;
import io.github.xanthic.core.AbstractCacheProvider;
import io.github.xanthic.core.delegate.GenericMapCacheDelegate;

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
