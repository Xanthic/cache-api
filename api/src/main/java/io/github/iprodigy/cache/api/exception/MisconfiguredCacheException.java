package io.github.iprodigy.cache.api.exception;

/**
 * Thrown when a cache specification issue arises
 * under {@link io.github.iprodigy.cache.api.domain.MisconfigurationPolicy#REJECT}
 */
public class MisconfiguredCacheException extends RuntimeException {

	public MisconfiguredCacheException(String message) {
		super(message);
	}

}
