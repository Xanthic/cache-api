package io.github.iprodigy.cache.core;

import io.github.iprodigy.cache.api.CacheProvider;
import io.github.iprodigy.cache.api.domain.ExpiryType;
import io.github.iprodigy.cache.api.domain.MisconfigurationPolicy;
import io.github.iprodigy.cache.api.exception.NoDefaultCacheImplementationException;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class CacheApiSettings {
	private static volatile CacheApiSettings INSTANCE;

	private final Map<Class<? extends CacheProvider>, CacheProvider> providers = Collections.synchronizedMap(new IdentityHashMap<>(16));
	private final AtomicReference<Class<? extends CacheProvider>> defaultCacheProvider = new AtomicReference<>();

	@Getter
	@Setter
	private volatile ExpiryType defaultExpiryType = ExpiryType.POST_ACCESS;

	@Getter
	@Setter
	private volatile MisconfigurationPolicy defaultMisconfigurationPolicy = MisconfigurationPolicy.IGNORE;

	private CacheApiSettings() {
		// restrict instantiation
	}

	public void setDefaultCacheProvider(@NotNull CacheProvider provider) {
		Class<? extends @NotNull CacheProvider> clazz = provider.getClass();
		this.registerCacheProvider(clazz, provider);
		this.defaultCacheProvider.set(clazz);
	}

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

	public void registerCacheProvider(@NotNull Class<? extends CacheProvider> cacheProviderClass, @Nullable CacheProvider cacheProvider) {
		providers.put(cacheProviderClass, cacheProvider);
		defaultCacheProvider.compareAndSet(null, cacheProviderClass);
	}

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

		loadImpl.accept("io.github.iprodigy.cache.provider.androidx.AndroidExpiringLruProvider");
		loadImpl.accept("io.github.iprodigy.cache.provider.androidx.AndroidLruProvider");
		loadImpl.accept("io.github.iprodigy.cache.provider.caffeine3.Caffeine3Provider");
		loadImpl.accept("io.github.iprodigy.cache.provider.caffeine.CaffeineProvider");
		loadImpl.accept("io.github.iprodigy.cache.provider.infinispan.InfinispanProvider");
		loadImpl.accept("io.github.iprodigy.cache.provider.expiringmap.ExpiringMapProvider");
		loadImpl.accept("io.github.iprodigy.cache.provider.guava.GuavaProvider");
		loadImpl.accept("io.github.iprodigy.cache.provider.ehcache.EhcacheProvider");
	}
}
