package io.github.xanthic.cache.api;

import io.github.xanthic.cache.api.domain.ExpiryType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Cache configuration settings that form a specification that requested implementations should satisfy.
 *
 * @param <K> the type of keys that form the cache
 * @param <V> the type of values that are contained in the cache
 */
public interface ICacheSpec<K, V> {

	/**
	 * The provider of a conforming cache instance.
	 * <p>
	 * Library developers should avoid specifying a non-null provider so that end users are given this choice.
	 *
	 * @return the specific provider that should be used to fulfill the desired cache specification
	 */
	@NotNull
	CacheProvider provider();

	/**
	 * The maximum capacity of the cache.
	 * <p>
	 * Null implies no constraint.
	 * Not-null values must be non-negative.
	 * <p>
	 * Specifying a not-null value is highly recommended.
	 *
	 * @return the maximum number of entries that may be contained in the cache
	 */
	@Nullable
	Long maxSize();

	/**
	 * The entry expiry time.
	 * <p>
	 * When specified, it is recommended to also specify {@link #expiryType()} and {@link #executor()}.
	 *
	 * @return the time to expiry for cache entries
	 */
	@Nullable
	Duration expiryTime();

	/**
	 * The entry expiration policy.
	 * <p>
	 * This has no effect unless {@link #expiryTime()} is specified.
	 *
	 * @return the type of expiration policy that should be applied to the cache
	 */
	@Nullable
	ExpiryType expiryType();

	/**
	 * The cache entry removal listener.
	 * <p>
	 * For timely eviction events, it is recommended to also specify {@link #executor()}.
	 *
	 * @return the listener to cache entry removals
	 */
	@Nullable
	RemovalListener<K, V> removalListener();

	/**
	 * The scheduler used by the cache.
	 * <p>
	 * When {@link #expiryTime()} is active, providing a not-null executor is highly recommended.
	 *
	 * @return the executor to optionally use for cache operations
	 */
	@Nullable
	ScheduledExecutorService executor();

	/**
	 * The forecasted contention of the cache.
	 * <p>
	 * If the cache will be mutated by many threads concurrently,
	 * this flag can be enabled to hint to certain providers to
	 * perform certain internal optimizations to boost performance,
	 * which could come at the cost of increased memory usage.
	 *
	 * @return whether providers should optimize for high contention
	 */
	@Nullable
	default Boolean highContention() {
		return null; // avoids breaking change
	}

}
