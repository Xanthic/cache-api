package io.github.xanthic.cache.api;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * The basic interface that various cache implementations must provide.
 * <p>
 * This interface can be thought as a superset of {@link java.util.Map};
 * less functionality is required here to achieve greater compatibility.
 * <p>
 * There cannot be duplicate keys, and null keys or values are not permitted.
 * <p>
 * Instances should be thread-safe, but no guarantee is made whether
 * this is achieved through fine-grained locking or blunt synchronization.
 * <p>
 * Implementations ought to implement expiry and size-based eviction, as defined in {@link ICacheSpec}.
 *
 * @param <K> The type of keys that form the cache
 * @param <V> The type of values contained in the cache
 */
public interface Cache<K, V> extends AutoCloseable {

	/**
	 * Obtains the value associated with the specified key.
	 *
	 * @param key the key whose mapped value should be queried
	 * @return the value associated with the key in the cache, or null if no such mapping was found
	 * @throws NullPointerException if the specified key is null
	 */
	@Nullable
	V get(@NotNull K key);

	/**
	 * Associates the specified key with the specified value,
	 * creating or replacing the mapping as needed.
	 *
	 * @param key   the key whose mapping should be created or updated
	 * @param value the value to be associated with the specified key
	 * @return the previous value associated with the key, or null if no prior mapping existed
	 * @throws NullPointerException if the specified key or value is null
	 */
	@Nullable
	V put(@NotNull K key, @NotNull V value);

	/**
	 * Deletes any mapping that may exist for the specified key.
	 *
	 * @param key the key whose mapping should be deleted
	 * @return the value in the removed mapping, or null if no mapping existed
	 * @throws NullPointerException if the specified key is null
	 */
	@Nullable
	V remove(@NotNull K key);

	/**
	 * Removes all entries from the cache.
	 */
	void clear();

	/**
	 * @return the estimated number of entries contained in the cache
	 */
	long size();

	/**
	 * Computes what value should be associated with the specified key, or null if the mapping should be removed.
	 *
	 * @param key         the key with which the specified value is to be associated
	 * @param computeFunc the function to compute a value
	 * @return the new value associated with the specified key, or null if none
	 * @throws NullPointerException if the specified key is null or the compute function is null
	 * @implNote atomicity is dependent on provider characteristics
	 */
	@Nullable
	V compute(@NotNull K key, @NotNull BiFunction<? super K, ? super V, ? extends V> computeFunc);

	/**
	 * Obtains the value currently associated with the specified key,
	 * or atomically stores the computed value if no prior mapping existed.
	 *
	 * @param key         the key whose mapping should be created or returned
	 * @param computeFunc the value supplier for a given key, if no mapping already existed
	 * @return the current (existing or computed) value associated with the key
	 * @throws NullPointerException if the specified key is null or the compute function is null
	 */
	V computeIfAbsent(@NotNull K key, @NotNull Function<K, V> computeFunc);

	/**
	 * Computes a new value for a specific key, if a mapping already existed.
	 * <p>
	 * If the compute function yields null, the mapping should be removed.
	 *
	 * @param key         the key whose mapping should be updated
	 * @param computeFunc the function to compute the new value for an already existing mapping
	 * @return the new value associated with the key, or null if none
	 * @throws NullPointerException if the specified key is null or the compute function is null
	 */
	@Nullable
	V computeIfPresent(@NotNull K key, @NotNull BiFunction<? super K, ? super V, ? extends V> computeFunc);

	/**
	 * Creates a mapping from the specified key to the specified value,
	 * if no mapping for the key already existed.
	 *
	 * @param key   the key whose mapping should be created or returned
	 * @param value the value that should be associated with the key if no prior mapping exists
	 * @return the previous value associated with the key, or null if no mapping already existed
	 * @throws NullPointerException if the specified key or value is null
	 */
	@Nullable
	V putIfAbsent(@NotNull K key, @NotNull V value);

	/**
	 * Associates the key with the specified value (or the result of the atomic merge function if a mapping already existed).
	 *
	 * @param key       the key whose mapping should be created or updated
	 * @param value     the value to be associated with the key or merged with the existing mapped value
	 * @param mergeFunc the function that takes the existing value and the new value to compute a merged value
	 * @return the latest value associated with the specified key
	 * @throws NullPointerException if the specified key or the value or mergeFunc is null
	 */
	V merge(@NotNull K key, @NotNull V value, @NotNull BiFunction<V, V, V> mergeFunc);

	/**
	 * Replaces the entry for the specified key only if it is currently mapped to some value.
	 *
	 * @param key   the key whose mapped value should be updated
	 * @param value the value to be associated with the specified key
	 * @return whether a replacement occurred (a prior mapping must have existed to return true)
	 * @throws NullPointerException if the specified key or value is null
	 */
	boolean replace(@NotNull K key, @NotNull V value);

	/**
	 * Replaces the value for the specified key only if it is currently mapped to the specified old value.
	 *
	 * @param key      the key whose mapped value should be updated
	 * @param oldValue the value expected to be already associated with the specified key
	 * @param newValue the value to be newly associated with the specified key
	 * @return whether a replacement occurred
	 * @throws NullPointerException if a specified key or newValue is null
	 */
	boolean replace(@NotNull K key, @NotNull V oldValue, @NotNull V newValue);

	/**
	 * Obtains the value associated with the specified key, or the passed default value if no mapping existed.
	 * <p>
	 * Note: Unlike {@link #computeIfAbsent(Object, Function)}, this method does <i>not</i> write the default value to the cache.
	 *
	 * @param key          the key whose mapped value should be queried
	 * @param defaultValue the default value to return if no mapping exists for the specified key
	 * @return the value mapped to the specified key, if present; otherwise, the specified default value
	 * @throws NullPointerException if the specified key is null
	 */
	@NotNull
	default V getOrDefault(@NotNull K key, @NotNull V defaultValue) {
		V value = get(key);
		return value != null ? value : defaultValue;
	}

	/**
	 * Copies all of the mappings from the specified map to this cache.
	 *
	 * @param map the map whose entries should be added to this cache
	 * @throws NullPointerException if the map is null, or the specified map contains null keys or values
	 * @implNote There is no behavior guarantee when there are concurrent updates to the map
	 */
	default void putAll(@NotNull Map<? extends K, ? extends V> map) {
		map.forEach(this::put);
	}

	/**
	 * Performs the specified action upon all entries within the cache.
	 *
	 * @param action the action to perform upon each entry
	 * @apiNote While this method is technically optional, all of the canonical
	 * implementations provided by Xanthic support this operation.
	 * @implSpec The iteration order of entries is not consistent (across cache different implementations),
	 * and should not be relied upon. Iteration may terminate early if the action yields an exception.
	 * @implNote This can be an inefficient operation that ought to be avoided;
	 * perhaps your data can be modeled differently to avoid this operation.
	 * @throws NullPointerException if the specified action is null
	 * @throws UnsupportedOperationException if the underlying cache provider does not support iteration over entries
	 */
	@ApiStatus.Experimental
	default void forEach(@NotNull BiConsumer<? super K, ? super V> action) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Avoid further usage of the cache once it has been closed;
	 * some implementations may throw exceptions while others are more tolerant.
	 */
	@Override
	default void close() {
		this.clear();
	}

}
