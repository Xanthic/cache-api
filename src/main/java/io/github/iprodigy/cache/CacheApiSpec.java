package io.github.iprodigy.cache;

import io.github.iprodigy.cache.providers.CacheProvider;
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

	private CacheProvider provider = CacheApiSettings.getInstance().getDefaultCacheProvider();

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
		data.validate();
		return data;
	}

}
