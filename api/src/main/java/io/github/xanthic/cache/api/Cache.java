package io.github.xanthic.cache.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * The basic interface that various cache implementations must provide.
 * <p>
 * This interface can be thought as a superset of {@link java.util.Map};
 * less functionality is required here to achieve greater compatibility.
 * <p>
 * There cannot be duplicate keys and null values are not necessarily supported.
 * <p>
 * Instances should be thread-safe, but no guarantee is made whether
 * this is achieved through fine-grained locking or blunt synchronization.
 * <p>
 * Implementations ought to implement expiry and size-based eviction, as defined in {@link ICacheSpec}.
 *
 * @param <K> The type of keys that form the cache
 * @param <V> The type of values contained in the cache
 */
public interface Cache<K, V> {

	/**
	 * Obtains the value associated with the specified key.
	 *
	 * @param key the key whose mapped value should be queried
	 * @return the value associated with the key in the cache, or null if no such mapping was found
	 * @throws NullPointerException if the specified key is null and this implementation does not permit null keys
	 */
	@Nullable
	V get(K key);

	/**
	 * Associates the specified key with the specified value,
	 * creating or replacing the mapping as needed.
	 *
	 * @param key   the key whose mapping should be created or updated
	 * @param value the value to be associated with the specified key
	 * @return the previous value associated with the key, or null if no prior mapping existed
	 * @throws NullPointerException if the specified key or value is null and this cache does not permit null keys or values
	 */
	@Nullable
	V put(K key, V value);

	/**
	 * Deletes any mapping that may exist for the specified key.
	 *
	 * @param key the key whose mapping should be deleted
	 * @return the value in the removed mapping, or null if no mapping existed
	 * @throws NullPointerException if the specified key is null and this cache does not permit null keys
	 */
	@Nullable
	V remove(K key);

	/**
	 * Removes all entries from the cache.
	 */
	void clear();

	/**
	 * @return the estimated number of entries contained in the cache
	 */
	long size();

	/**
	 * Obtains the value currently associated with the specified key,
	 * or atomically stores the computed value if no prior mapping existed.
	 *
	 * @param key         the key whose mapping should be created or returned
	 * @param computeFunc the value supplier for a given key, if no mapping already existed
	 * @return the current (existing or computed) value associated with the key
	 * @throws NullPointerException if the specified key is null and this cache does not permit null keys, or the compute function is null
	 */
	V computeIfAbsent(K key, @NotNull Function<K, V> computeFunc);

	/**
	 * Creates a mapping from the specified key to the specified value,
	 * if no mapping for the key already existed.
	 *
	 * @param key   the key whose mapping should be created or returned
	 * @param value the value that should be associated with the key if no prior mapping exists
	 * @return the previous value associated with the key, or null if no mapping already existed
	 * @throws NullPointerException if the specified key or value is null and this cache does not permit null keys or values
	 */
	@Nullable
	V putIfAbsent(K key, V value);

	/**
	 * Associates the key with the specified value (or the result of the atomic merge function if a mapping already existed).
	 *
	 * @param key       the key whose mapping should be created or updated
	 * @param value     the value to be associated with the key or merged with the existing mapped value
	 * @param mergeFunc the function that takes the existing value and the new value to compute a merged value
	 * @return the latest value associated with the specified key
	 * @throws NullPointerException if the specified key is null and this cache does not support null keys or the value or mergeFunc is null
	 */
	V merge(K key, V value, @NotNull BiFunction<V, V, V> mergeFunc);

	/**
	 * Replaces the entry for the specified key only if it is currently mapped to some value.
	 *
	 * @param key   the key whose mapped value should be updated
	 * @param value the value to be associated with the specified key
	 * @return whether a replacement occurred (a prior mapping must have existed to return true)
	 */
	boolean replace(K key, V value);

	/**
	 * Replaces the value for the specified key only if it is currently mapped to the specified old value.
	 *
	 * @param key      the key whose mapped value should be updated
	 * @param oldValue the value expected to be already associated with the specified key
	 * @param newValue the value to be newly associated with the specified key
	 * @return whether a replacement occurred
	 */
	boolean replace(K key, V oldValue, V newValue);

	/**
	 * Copies all of the mappings from the specified map to this cache.
	 *
	 * @param map the map whose entries should be added to this cache
	 * @throws NullPointerException if the specified map is null, or if this cache does not permit null keys or values, and the specified map contains null keys or values
	 * @implNote There is no behavior guarantee when there are concurrent updates to the map
	 */
	default void putAll(@NotNull Map<? extends K, ? extends V> map) {
		map.forEach(this::put);
	}

}
