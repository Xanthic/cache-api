package io.github.iprodigy.cache.api.domain;

/**
 * The type of expiration policy to use.
 */
public enum ExpiryType {

	/**
	 * Entries should expire when no accesses occur during a specific time period.
	 */
	POST_ACCESS,

	/**
	 * Entries should expire after a specific time period has passed since the entry creation.
	 */
	POST_WRITE

}
