package io.github.xanthic.cache.core;

import io.github.xanthic.cache.api.CacheProvider;
import io.github.xanthic.cache.api.domain.ExpiryType;
import io.github.xanthic.cache.api.domain.MisconfigurationPolicy;
import io.github.xanthic.cache.api.exception.MisconfiguredCacheException;
import org.jetbrains.annotations.ApiStatus;

import java.time.Duration;
import java.util.function.BiConsumer;

/**
 * Provides helper methods commonly used for implementing {@link CacheProvider}.
 */
public abstract class AbstractCacheProvider implements CacheProvider {

	protected void handleUnsupportedExpiry(Duration expiryTime) {
		if (expiryTime != null && CacheApiSettings.getInstance().getDefaultMisconfigurationPolicy() == MisconfigurationPolicy.REJECT)
			throw new MisconfiguredCacheException("Expiration is not supported by this backing cache configuration");
	}

	protected ExpiryType getExpiryType(ExpiryType type) {
		if (type != null) return type;

		ExpiryType et = this.preferredType();
		if (et == null || CacheApiSettings.getInstance().getDefaultMisconfigurationPolicy() == MisconfigurationPolicy.REJECT)
			throw new MisconfiguredCacheException("Expiry time was set without an expiry type specified, even as a default");
		return et;
	}

	protected void handleExpiration(Duration time, ExpiryType type, BiConsumer<Duration, ExpiryType> registrar) {
		if (time != null) registrar.accept(time, getExpiryType(type));
	}

	protected ExpiryType preferredType() {
		return ExpiryType.POST_ACCESS; // LRU
	}
	
	@ApiStatus.Internal
	public int getDiscoveryOrder() {
		return -1;
	}
	

}
