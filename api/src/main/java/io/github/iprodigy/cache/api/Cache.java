package io.github.iprodigy.cache.api;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface Cache<K, V> {

	V get(K key);

	V put(K key, V value);

	V remove(K key);

	void clear();

	long size();

	V computeIfAbsent(K key, Function<K, V> computeFunc);

	V putIfAbsent(K key, V value);

	V merge(K key, V value, BiFunction<V, V, V> mergeFunc);

}
