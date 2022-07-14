package io.github.xanthic.cache.api.exception;

import io.github.xanthic.cache.api.domain.MisconfigurationPolicy;

/**
 * Thrown when a cache specification issue arises
 * under {@link MisconfigurationPolicy#REJECT}
 */
public class MisconfiguredCacheException extends RuntimeException {

	public MisconfiguredCacheException(String message) {
		super(message);
	}

}
