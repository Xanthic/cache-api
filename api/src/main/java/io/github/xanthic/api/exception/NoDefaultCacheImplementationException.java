package io.github.xanthic.api.exception;

/**
 * Thrown when a cache is to be built with no provider specified and no default provider was found.
 */
public class NoDefaultCacheImplementationException extends RuntimeException {

	public NoDefaultCacheImplementationException(String message) {
		super(message);
	}

}
