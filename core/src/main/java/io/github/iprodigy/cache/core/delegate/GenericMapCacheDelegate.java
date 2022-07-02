package io.github.iprodigy.cache.core.delegate;

import io.github.iprodigy.cache.api.Cache;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

@Data
public class GenericMapCacheDelegate<K, V> implements Cache<K, V> {
	private final Map<K, V> map;

	@Override
	public V get(K key) {
		return map.get(key);
	}

	@Override
	public V put(K key, V value) {
		return map.put(key, value);
	}

	@Override
	public V computeIfAbsent(K key, @NotNull Function<K, V> computeFunc) {
		return map.computeIfAbsent(key, computeFunc);
	}

	@Override
	public V remove(K key) {
		return map.remove(key);
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public long size() {
		return map.size();
	}

	@Override
	public V putIfAbsent(K key, V value) {
		return map.putIfAbsent(key, value);
	}

	@Override
	public V merge(K key, V value, @NotNull BiFunction<V, V, V> mergeFunc) {
		return map.merge(key, value, mergeFunc);
	}
}
