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
			throw new MisconfiguredCacheException("Expiration is not natively supported by this cache implementation");
	}

	protected void handleExpiration(Duration time, ExpiryType type, BiConsumer<Duration, ExpiryType> registrar) {
		if (time == null) return;
		final ExpiryType et = type != null ? type : CacheApiSettings.getInstance().getDefaultExpiryType();
		if (et == ExpiryType.POST_WRITE)
			registrar.accept(time, ExpiryType.POST_WRITE);
		else if (et == ExpiryType.POST_ACCESS)
			registrar.accept(time, ExpiryType.POST_ACCESS);
		else if (CacheApiSettings.getInstance().getDefaultMisconfigurationPolicy() != MisconfigurationPolicy.REJECT)
			registrar.accept(time, preferredType());
		else
			throw new MisconfiguredCacheException("Expiry time was set without an expiry type specified, even as a default");
	}

	protected ExpiryType preferredType() {
		return ExpiryType.POST_ACCESS; // LRU
	}

}
