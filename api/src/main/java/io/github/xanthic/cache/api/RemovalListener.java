package io.github.xanthic.cache.api;

import io.github.xanthic.cache.api.domain.RemovalCause;

/**
 * Listens to entry removals in the cache that may occur for various reasons.
 *
 * @param <K> the type of keys that form the cache
 * @param <V> the type of values that are contained in the cache
 */
@FunctionalInterface
public interface RemovalListener<K, V> {

	/**
	 * Called when an entry is removed.
	 * <p>
	 * No guarantee is made whether this is executed synchronously or asynchronously.
	 * <p>
	 * For optimal compatibility across backing implementations, prefer using
	 * {@link RemovalCause#isEviction()} to checking against specific states.
	 *
	 * @param key   the key whose mapping was removed or updated
	 * @param value the mapped value that was removed
	 * @param cause the reason for the removal
	 */
	void onRemoval(K key, V value, RemovalCause cause);

}
