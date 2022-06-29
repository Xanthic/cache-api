package io.github.iprodigy.cache.api.domain;

/**
 * The policy type that dictates how providers should react to cache specification issues.
 */
public enum MisconfigurationPolicy {

	/**
	 * Halts cache building (e.g., via exception) upon a cache specification issue.
	 *
	 * @see io.github.iprodigy.cache.api.exception.MisconfiguredCacheException
	 */
	REJECT,

	/**
	 * Attempts to ignore specification issues when building caches.
	 * <p>
	 * This can result in certain features of the cache specification being skipped in order to proceed.
	 */
	IGNORE

}
