package io.github.xanthic.api.exception;

import io.github.xanthic.api.domain.MisconfigurationPolicy;

/**
 * Thrown when a cache specification issue arises
 * under {@link MisconfigurationPolicy#REJECT}
 */
public class MisconfiguredCacheException extends RuntimeException {

	public MisconfiguredCacheException(String message) {
		super(message);
	}

}
