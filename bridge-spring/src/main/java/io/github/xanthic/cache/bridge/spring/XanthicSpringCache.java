package io.github.xanthic.cache.bridge.spring;

import io.github.xanthic.cache.api.Cache;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.support.AbstractValueAdaptingCache;

import java.util.concurrent.Callable;

public class XanthicSpringCache extends AbstractValueAdaptingCache {
	private final String name;
	private final Cache<Object, Object> cache;

	public XanthicSpringCache(String name, Cache<Object, Object> cache) {
		super(true);
		this.name = name;
		this.cache = cache;
	}

	@Override
	public @NotNull String getName() {
		return name;
	}

	@Override
	public @NotNull Object getNativeCache() {
		return cache;
	}

	@Override
	public <T> T get(@NotNull Object key, @NotNull Callable<T> valueLoader) {
		return null;
	}

	@Override
	public void put(@NotNull Object key, Object value) {
		cache.put(key, value);
	}

	@Override
	public void evict(@NotNull Object key) {
		cache.remove(key);
	}

	@Override
	public void clear() {
		cache.clear();
	}

	@Override
	protected Object lookup(@NotNull Object key) {
		return cache.get(key);
	}
}


