package io.github.xanthic.core;

import io.github.xanthic.api.CacheProvider;
import io.github.xanthic.api.domain.ExpiryType;
import io.github.xanthic.api.domain.MisconfigurationPolicy;
import io.github.xanthic.api.exception.NoDefaultCacheImplementationException;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Holds a registry of default settings and cache providers.
 */
public final class CacheApiSettings {
	private static volatile CacheApiSettings INSTANCE;

	private final Map<Class<? extends CacheProvider>, CacheProvider> providers = Collections.synchronizedMap(new IdentityHashMap<>(16));
	private final AtomicReference<Class<? extends CacheProvider>> defaultCacheProvider = new AtomicReference<>();

	/**
	 * The default expiry type to use when none is specified.
	 *
	 * @see ExpiryType
	 */
	@Getter
	@Setter
	private volatile ExpiryType defaultExpiryType = ExpiryType.POST_ACCESS;

	/**
	 * The default misconfiguration policy.
	 *
	 * @see MisconfigurationPolicy
	 */
	@Getter
	@Setter
	private volatile MisconfigurationPolicy defaultMisconfigurationPolicy = MisconfigurationPolicy.IGNORE;

	private CacheApiSettings() {
		// restrict instantiation
	}

	/**
	 * Sets the default cache provider to use for caches that don't explicitly specify a provider to use.
	 *
	 * @param provider the cache provider to be the new default
	 * @throws NullPointerException if the provider argument is null
	 */
	public void setDefaultCacheProvider(@NotNull CacheProvider provider) {
		Class<? extends @NotNull CacheProvider> clazz = provider.getClass();
		this.registerCacheProvider(clazz, provider);
		this.defaultCacheProvider.set(clazz);
	}

	/**
	 * Obtains the default cache provider.
	 *
	 * @return an instance of the default cache provider
	 * @throws NoDefaultCacheImplementationException if no default has been set
	 * @see #setDefaultCacheProvider(CacheProvider)
	 * @see #registerCacheProvider(Class, CacheProvider)
	 */
	@NotNull
	@SneakyThrows
	public CacheProvider getDefaultCacheProvider() {
		Class<? extends CacheProvider> clazz = this.defaultCacheProvider.get();
		if (clazz == null) {
			throw new NoDefaultCacheImplementationException("default cache provider is not set, no cache implementations available!");
		}

		CacheProvider provider = providers.get(clazz);
		if (provider == null) {
			provider = clazz.getDeclaredConstructor().newInstance();
			providers.put(clazz, provider);
		}

		return provider;
	}

	/**
	 * Registers an available provider for the cache settings.
	 * <p>
	 * Further, the default cache provider will be set to this cache provider, if the default has not yet been set.
	 * <p>
	 * Note: the cacheProvider parameter can only be null if there is a public no-args constructor for the provider.
	 * This allows for lazy initialization of the provider only if it is needed.
	 *
	 * @param cacheProviderClass the class of the provider
	 * @param cacheProvider      an instance of the provider
	 * @throws NullPointerException if cacheProviderClass is null
	 */
	public void registerCacheProvider(@NonNull Class<? extends CacheProvider> cacheProviderClass, @Nullable CacheProvider cacheProvider) {
		providers.put(cacheProviderClass, cacheProvider);
		defaultCacheProvider.compareAndSet(null, cacheProviderClass);
	}

	/**
	 * @return the cache api settings singleton
	 */
	@NotNull
	public static CacheApiSettings getInstance() {
		if (INSTANCE == null) {
			synchronized (CacheApiSettings.class) {
				if (INSTANCE == null) {
					CacheApiSettings cacheApiSettings = new CacheApiSettings();
					populateProviders(cacheApiSettings);
					INSTANCE = cacheApiSettings;
				}
			}
		}
		return INSTANCE;
	}

	private static void populateProviders(CacheApiSettings cacheApiSettings) {
		Consumer<String> loadImpl = (providerClass) -> {
			try {
				Class<? extends CacheProvider> clazz = Class.forName(providerClass).asSubclass(CacheProvider.class);
				cacheApiSettings.registerCacheProvider(clazz, null); // lazy, init if needed
			} catch (Exception ignored) {
			}
		};

		loadImpl.accept("io.github.xanthic.provider.androidx.AndroidExpiringLruProvider");
		loadImpl.accept("io.github.xanthic.provider.androidx.AndroidLruProvider");
		loadImpl.accept("io.github.xanthic.provider.caffeine3.Caffeine3Provider");
		loadImpl.accept("io.github.xanthic.provider.caffeine.CaffeineProvider");
		loadImpl.accept("io.github.xanthic.provider.cache2k.Cache2kProvider");
		loadImpl.accept("io.github.xanthic.provider.infinispan.InfinispanProvider");
		loadImpl.accept("io.github.xanthic.provider.expiringmap.ExpiringMapProvider");
		loadImpl.accept("io.github.xanthic.provider.guava.GuavaProvider");
		loadImpl.accept("io.github.xanthic.provider.ehcache.EhcacheProvider");
	}
}
