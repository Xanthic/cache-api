package io.github.iprodigy.cache.providers;

import io.github.iprodigy.cache.Cache;
import io.github.iprodigy.cache.ExpiryType;
import io.github.iprodigy.cache.RemovalCause;
import io.github.iprodigy.cache.RemovalListener;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class ExpiringMapProvider extends AbstractCacheProvider {
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
		if (removalListener != null) builder.<K, V>expirationListener((key, value) -> removalListener.onRemoval(key, value, RemovalCause.OTHER));
		handleExpiration(expiryTime, expiryType, (time, type) -> {
			builder.expiration(time.toNanos(), TimeUnit.NANOSECONDS);
			if (type == ExpiryType.POST_WRITE)
				builder.expirationPolicy(ExpirationPolicy.CREATED);
			else
				builder.expirationPolicy(ExpirationPolicy.ACCESSED);
		});

		return new GenericMapCacheDelegate<>(builder.build());
	}
}
