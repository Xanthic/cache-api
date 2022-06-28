package io.github.iprodigy.cache.api;

import io.github.iprodigy.cache.api.domain.RemovalCause;

@FunctionalInterface
public interface RemovalListener<K, V> {
	void onRemoval(K key, V value, RemovalCause cause);
}
