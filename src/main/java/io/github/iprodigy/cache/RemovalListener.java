package io.github.iprodigy.cache;

@FunctionalInterface
public interface RemovalListener<K, V> {
	void onRemoval(K key, V value, RemovalCause cause);
}
