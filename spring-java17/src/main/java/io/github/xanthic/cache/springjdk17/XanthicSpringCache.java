package io.github.xanthic.cache.springjdk17;

import io.github.xanthic.cache.api.Cache;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.support.AbstractValueAdaptingCache;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

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
		return (T) fromStoreValue(cache.computeIfAbsent(key, k -> getSynchronized(key, valueLoader)));
	}

	private synchronized <T> Object getSynchronized(Object key, Callable<T> valueLoader) {
		T value;
		try {
			value = valueLoader.call();
		} catch (Exception e) {
			throw new ValueRetrievalException(key, valueLoader, e);
		}
		return toStoreValue(value);
	}

	@Override
	public void put(@NotNull Object key, Object value) {
		cache.put(key, toStoreValue(value));
	}

	@Override
	public ValueWrapper putIfAbsent(@NotNull Object key, Object value) {
		return toValueWrapper(cache.putIfAbsent(key, toStoreValue(value)));
	}

	@Override
	public void evict(@NotNull Object key) {
		cache.remove(key);
	}

	@Override
	public boolean evictIfPresent(@NotNull Object key) {
		return cache.remove(key) != null;
	}

	@Override
	public void clear() {
		cache.clear();
	}

	@Override
	protected Object lookup(@NotNull Object key) {
		return cache.get(key);
	}

	@Override
	public CompletableFuture<?> retrieve(@NotNull Object key) {
		Object value = lookup(key);
		if (value == null) return null;
		return CompletableFuture.completedFuture(toValueWrapper(value));
	}

	@NotNull
	@Override
	@SuppressWarnings("unchecked")
	public <T> CompletableFuture<T> retrieve(@NotNull Object key, @NotNull Supplier<CompletableFuture<T>> valueLoader) {
		return CompletableFuture.supplyAsync(
			() -> (T) fromStoreValue(
				cache.computeIfAbsent(key, k -> toStoreValue(valueLoader.get().join()))
			)
		);
	}
}
