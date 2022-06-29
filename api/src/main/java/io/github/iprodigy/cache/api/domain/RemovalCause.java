package io.github.iprodigy.cache.api.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The reason for a cache entry being removed.
 *
 * @see io.github.iprodigy.cache.api.RemovalListener
 */
@RequiredArgsConstructor
public enum RemovalCause {

	/**
	 * The maximum capacity constraint of the cache was hit.
	 */
	SIZE(true),

	/**
	 * The cache entry reached expiration, according to the policy in the specification.
	 */
	TIME(true),

	/**
	 * The value associated with the key was replaced by a new value.
	 */
	REPLACED(false),

	/**
	 * The entry was fully removed from the cache.
	 */
	MANUAL(false),

	/**
	 * An extraneous eviction occurred (e.g., due to garbage collection).
	 */
	OTHER(true);

	/**
	 * Whether the removal can be classified as an eviction (typically size-based or time-based)
	 */
	@Getter
	private final boolean eviction;

}
