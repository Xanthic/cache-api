package io.github.iprodigy.cache.core;

import io.github.iprodigy.cache.api.Cache;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class AbstractCache<K, V> implements Cache<K, V> {

	@Override
	public V computeIfAbsent(K key, @NotNull Function<K, V> computeFunc) {
		synchronized (getLock()) {
			V old = this.get(key);
			if (old != null) return old;
			return this.put(key, computeFunc.apply(key));
		}
	}

	@Override
	public V putIfAbsent(K key, V value) {
		synchronized (getLock()) {
			V old = this.get(key);
			if (old == null) {
				this.put(key, value);
			}
			return old;
		}
	}

	@Override
	public V merge(K key, V value, @NotNull BiFunction<V, V, V> mergeFunc) {
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
