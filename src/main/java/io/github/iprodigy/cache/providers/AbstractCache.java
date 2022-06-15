package io.github.iprodigy.cache.providers;

import io.github.iprodigy.cache.Cache;

import java.util.function.BiFunction;
import java.util.function.Function;

abstract class AbstractCache<K, V> implements Cache<K, V> {

	public V computeIfAbsent(K key, Function<K, V> computeFunc) {
		synchronized (getLock()) {
			V old = this.get(key);
			if (old != null) return old;
			return this.put(key, computeFunc.apply(key));
		}
	}

	public V putIfAbsent(K key, V value) {
		synchronized (getLock()) {
			V old = this.get(key);
			if (old == null) {
				this.put(key, value);
			}
			return old;
		}
	}

	public V merge(K key, V value, BiFunction<V, V, V> mergeFunc) {
		synchronized (getLock()) {
			V old = putIfAbsent(key, value);
			if (old == null) return value;
			return this.put(key, mergeFunc.apply(old, value));
		}
	}

	protected Object getLock() {
		return this;
	}

}
