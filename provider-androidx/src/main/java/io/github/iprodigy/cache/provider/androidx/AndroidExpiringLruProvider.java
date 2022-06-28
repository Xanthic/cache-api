package io.github.iprodigy.cache.provider.androidx;

import androidx.collection.LruCache;
import io.github.iprodigy.cache.api.Cache;
import io.github.iprodigy.cache.api.ICacheSpec;
import io.github.iprodigy.cache.core.AbstractCacheProvider;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class AndroidExpiringLruProvider extends AbstractCacheProvider {
	@Override
	public <K, V> Cache<K, V> build(ICacheSpec<K, V> spec) {
		ScheduledExecutorService executor = spec.executor();
		Duration expiryTime = spec.expiryTime();
		if (executor == null) handleUnsupportedExpiry(expiryTime);
		LruCache<K, V> cache = AndroidLruProvider.build(spec.maxSize(), spec.removalListener());
		if (expiryTime == null) return new LruDelegate<>(cache);
		ScheduledExecutorService exec = executor != null ? executor : Executors.newSingleThreadScheduledExecutor();
		return new ExpiringLruDelegate<>(cache, expiryTime.toNanos(), getExpiryType(spec.expiryType()), exec);
	}
}
