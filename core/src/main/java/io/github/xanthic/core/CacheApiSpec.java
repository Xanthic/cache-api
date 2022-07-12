package io.github.xanthic.core;

import io.github.xanthic.api.CacheProvider;
import io.github.xanthic.api.ICacheSpec;
import io.github.xanthic.api.RemovalListener;
import io.github.xanthic.api.domain.ExpiryType;
import io.github.xanthic.api.exception.MisconfiguredCacheException;
import io.github.xanthic.api.exception.NoDefaultCacheImplementationException;
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

/**
 * Fluent implementation of {@link ICacheSpec}.
 * <p>
 * Use {@link #process(Consumer)} to obtain validated instances of the spec.
 *
 * @param <K> the type of keys that form the cache
 * @param <V> the type of values that are contained in the cache
 */
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
	 * Ensures the configured specification is valid.
	 *
	 * @throws NullPointerException        if a provider is not specified and no default provider has been set
	 * @throws MisconfiguredCacheException if the cache settings are invalid (e.g., negative max size or expiry time)
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

	/**
	 * Constructs a validated implementation of {@link ICacheSpec}.
	 *
	 * @param spec consumer in which the desired cache settings should be specified
	 * @param <K>  the type of keys that form the cache
	 * @param <V>  the type of values that are contained in the cache
	 * @return CacheApiSpec
	 * @throws NoDefaultCacheImplementationException if a provider is not specified and no default provider has been set
	 * @throws MisconfiguredCacheException           if the cache settings are invalid (e.g., negative max size or expiry time)
	 */
	@NotNull
	public static <K, V> CacheApiSpec<K, V> process(@NotNull Consumer<CacheApiSpec<K, V>> spec) {
		CacheApiSpec<K, V> data = new CacheApiSpec<>();
		spec.accept(data);

		// set / init default cache provider if nothing is set
		if (data.provider() == null) {
			data.provider(CacheApiSettings.getInstance().getDefaultCacheProvider());
			log.debug("no cache provider set, cache defaults to {}!", data.provider.getClass().getCanonicalName());
		}

		data.validate();
		return data;
	}

}
