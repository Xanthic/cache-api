package io.github.xanthic.cache.provider.infinispan;

import io.github.xanthic.cache.api.Cache;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

@Value
class InfinispanDelegate<K, V> implements Cache<K, V> {
	org.infinispan.Cache<K, V> cache;

	@Override
	public V get(K key) {
		return cache.get(key);
	}

	@Override
	public V put(K key, V value) {
		return cache.put(key, value);
	}

	@Override
	public V remove(K key) {
		return cache.remove(key);
	}

	@Override
	public void clear() {
		cache.clear();
	}

	@Override
	public long size() {
		return cache.size();
	}

	@Override
	public V computeIfAbsent(K key, @NotNull Function<K, V> computeFunc) {
		return cache.computeIfAbsent(key, computeFunc);
	}

	@Override
	public V putIfAbsent(K key, V value) {
		return cache.putIfAbsent(key, value);
	}

	@Override
	public V merge(K key, V value, @NotNull BiFunction<V, V, V> mergeFunc) {
		return cache.merge(key, value, mergeFunc);
	}

	@Override
	public boolean replace(K key, V value) {
		return cache.replace(key, value) != null;
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		return cache.replace(key, oldValue, newValue);
	}

	@Override
	public void putAll(@NotNull Map<? extends K, ? extends V> map) {
		cache.putAll(map);
	}
}
