package io.github.iprodigy.cache.providers;

import io.github.iprodigy.cache.Cache;
import io.github.iprodigy.cache.ExpiryType;
import io.github.iprodigy.cache.ICacheSpec;
import io.github.iprodigy.cache.RemovalListener;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

@FunctionalInterface
public interface CacheProvider {
	<K, V> Cache<K, V> build(
		@Nullable Long maxSize,
		@Nullable Duration expiryTime,
		@Nullable ExpiryType expiryType,
		@Nullable RemovalListener<K, V> removalListener,
		@Nullable ScheduledExecutorService executor
	);

	default <K, V> Cache<K, V> build(ICacheSpec<K, V> spec) {
		return build(spec.maxSize(), spec.expiryTime(), spec.expiryType(), spec.removalListener(), spec.executor());
	}
}
