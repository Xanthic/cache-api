package io.github.iprodigy.cache.core;

import io.github.iprodigy.cache.api.CacheProvider;
import io.github.iprodigy.cache.api.ICacheSpec;
import io.github.iprodigy.cache.api.RemovalListener;
import io.github.iprodigy.cache.api.domain.ExpiryType;
import io.github.iprodigy.cache.api.exception.MisconfiguredCacheException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

@Data
@Slf4j
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CacheApiSpec<K, V> implements ICacheSpec<K, V> {

	private CacheProvider provider;

	private Long maxSize;

	private Duration expiryTime;

	private ExpiryType expiryType;

	private RemovalListener<K, V> removalListener;

	private ScheduledExecutorService executor;

	/**
	 * Ensure the config is valid
	 */
	public void validate() {
		Objects.requireNonNull(provider, "provider may not be null!");

		if (maxSize != null && maxSize < 0)
			throw new MisconfiguredCacheException("maxSize may not be negative!");

		if (expiryTime != null && expiryTime.isNegative())
			throw new MisconfiguredCacheException("expiryTime may not be negative!");

		if (expiryTime != null && expiryType == null)
			log.warn("Cache specification enables expiry time but does not specify ExpiryType");
	}

	public static <K, V> @NotNull CacheApiSpec<K, V> process(@NotNull Consumer<CacheApiSpec<K, V>> spec) {
		CacheApiSpec<K, V> data = new CacheApiSpec<>();
		spec.accept(data);

		// set / init default cache provider if nothing is set
		if (data.provider() == null) {
			data.provider(CacheApiSettings.getInstance().getDefaultCacheProvider());
			log.warn("no cache provider set, cache defaults to {}!", CacheApiSettings.getInstance().getDefaultCacheProvider().getClass().getCanonicalName());
		}

		data.validate();
		return data;
	}

}
