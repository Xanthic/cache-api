package io.github.iprodigy.cache.providers;

import io.github.iprodigy.cache.Cache;
import io.github.iprodigy.cache.CacheApiSettings;
import io.github.iprodigy.cache.ExpiryType;
import io.github.iprodigy.cache.MisconfigurationPolicy;
import io.github.iprodigy.cache.MisconfiguredCacheException;
import io.github.iprodigy.cache.RemovalCause;
import io.github.iprodigy.cache.RemovalListener;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExpiringMapProvider implements CacheProvider {
	@Override
	public <K, V> Cache<K, V> build(
		@Nullable Long maxSize,
		@Nullable Duration expiryTime,
		@Nullable ExpiryType expiryType,
		@Nullable RemovalListener<K, V> removalListener,
		@Nullable ScheduledExecutorService executor
	) {
		ExpiringMap.Builder<Object, Object> builder = ExpiringMap.builder();
		if (maxSize != null) builder.maxSize(maxSize.intValue());
		if (expiryTime != null) {
			builder.expiration(expiryTime.toNanos(), TimeUnit.NANOSECONDS);

			ExpiryType type = expiryType != null ? expiryType : CacheApiSettings.getInstance().getDefaultExpiryType();
			if (type == ExpiryType.POST_WRITE)
				builder.expirationPolicy(ExpirationPolicy.CREATED);
			else if (type == ExpiryType.POST_ACCESS || CacheApiSettings.getInstance().getDefaultMisconfigurationPolicy() != MisconfigurationPolicy.REJECT)
				builder.expirationPolicy(ExpirationPolicy.ACCESSED);
			else
				throw new MisconfiguredCacheException("Expiry time was set without an expiry type specified, even as a default");
		}
		if (removalListener != null) builder.<K, V>expirationListener((key, value) -> removalListener.onRemoval(key, value, RemovalCause.OTHER));

		return new GenericMapCacheDelegate<>(builder.build());
	}
}
