package io.github.xanthic.cache.spring;

import io.github.xanthic.cache.api.Cache;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.support.AbstractValueAdaptingCache;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

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

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(@NotNull Object key, @NotNull Callable<T> valueLoader) {
		return (T) cache.computeIfAbsent(key, k -> getSynchronized(key, valueLoader));
	}

	@SuppressWarnings("unchecked")
	private synchronized <T> T getSynchronized(Object key, Callable<T> valueLoader) {
		ValueWrapper result = get(key);

		if (result != null) {
			return (T) result.get();
		}

		T value;
		try {
			value = valueLoader.call();
		} catch (Exception e) {
			throw new ValueRetrievalException(key, valueLoader, e);
		}
		return value;
	}

	@Override
	public void put(@NotNull Object key, Object value) {
		cache.put(key, toStoreValue(value));
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


