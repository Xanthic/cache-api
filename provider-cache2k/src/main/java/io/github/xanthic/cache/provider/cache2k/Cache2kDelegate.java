package io.github.xanthic.cache.provider.cache2k;

import io.github.xanthic.cache.core.delegate.GenericMapCacheDelegate;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.cache2k.Cache;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@Value
@EqualsAndHashCode(callSuper = false)
class Cache2kDelegate<K, V> extends GenericMapCacheDelegate<K, V> {
	Cache<K, V> cache;

	public Cache2kDelegate(Cache<K, V> cache) {
		super(cache.asMap());
		this.cache = cache;
	}

	@Override
	public V get(K key) {
		return cache.get(key);
	}

	@Override
	public V put(K key, V value) {
		return cache.peekAndPut(key, value);
	}

	@Override
	public V remove(K key) {
		return cache.peekAndRemove(key);
	}

	@Override
	public void clear() {
		cache.clear();
	}

	@Override
	public V computeIfAbsent(K key, @NotNull Function<K, V> computeFunc) {
		return cache.computeIfAbsent(key, computeFunc);
	}
}
