package io.github.iprodigy.cache.providers;

import io.github.iprodigy.cache.CacheApiSettings;
import io.github.iprodigy.cache.ExpiryType;
import io.github.iprodigy.cache.MisconfigurationPolicy;
import io.github.iprodigy.cache.MisconfiguredCacheException;

import java.time.Duration;
import java.util.function.BiConsumer;

abstract class AbstractCacheProvider implements CacheProvider {

	protected void handleUnsupportedExpiry(Duration expiryTime) {
		if (expiryTime != null && CacheApiSettings.getInstance().getDefaultMisconfigurationPolicy() == MisconfigurationPolicy.REJECT)
			throw new MisconfiguredCacheException("Expiration is not supported by this backing cache configuration");
	}

	protected ExpiryType getExpiryType(ExpiryType type) {
		ExpiryType et = type != null ? type : CacheApiSettings.getInstance().getDefaultExpiryType();
		if (et == null) {
			et = this.preferredType();
			if (et == null || CacheApiSettings.getInstance().getDefaultMisconfigurationPolicy() == MisconfigurationPolicy.REJECT)
				throw new MisconfiguredCacheException("Expiry time was set without an expiry type specified, even as a default");
		}
		return et;
	}

	protected void handleExpiration(Duration time, ExpiryType type, BiConsumer<Duration, ExpiryType> registrar) {
		if (time != null) registrar.accept(time, getExpiryType(type));
	}

	protected ExpiryType preferredType() {
		return ExpiryType.POST_ACCESS; // LRU
	}

}
