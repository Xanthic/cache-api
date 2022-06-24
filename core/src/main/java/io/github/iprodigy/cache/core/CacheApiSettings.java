package io.github.iprodigy.cache.core;

import io.github.iprodigy.cache.api.CacheProvider;
import io.github.iprodigy.cache.api.domain.ExpiryType;
import io.github.iprodigy.cache.api.domain.MisconfigurationPolicy;
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

public enum CacheApiSettings {
	INSTANCE; // thread-safe singleton

	private final Map<Class<? extends CacheProvider>, CacheProvider> providers = Collections.synchronizedMap(new IdentityHashMap<>(16));
	private final AtomicReference<Class<? extends CacheProvider>> defaultCacheProvider = new AtomicReference<>();

	@Getter
	@Setter
	private volatile ExpiryType defaultExpiryType = ExpiryType.POST_ACCESS;

	@Getter
	@Setter
	private volatile MisconfigurationPolicy defaultMisconfigurationPolicy = MisconfigurationPolicy.IGNORE;

	public void setDefaultCacheProvider(@NotNull CacheProvider provider) {
		this.defaultCacheProvider.set(this.registerCacheProvider(provider.getClass(), provider));
	}

	@NotNull
	@SneakyThrows
	public CacheProvider getDefaultCacheProvider() {
		Class<? extends CacheProvider> clazz = this.defaultCacheProvider.get();
		CacheProvider provider = providers.get(clazz);
		if (provider == null) {
			provider = clazz.getDeclaredConstructor().newInstance();
			providers.put(clazz, provider);
		}

		return provider;
	}
	public Class<? extends CacheProvider> registerCacheProvider(@NotNull Class<? extends CacheProvider> cacheProviderClass, @Nullable CacheProvider cacheProvider) {
		providers.put(cacheProviderClass, cacheProvider);
		defaultCacheProvider.compareAndSet(null, cacheProviderClass);
		return cacheProviderClass;
	}

	public static CacheApiSettings getInstance() {
		return INSTANCE;
	}

	static {
		Consumer<String> loadImpl = (providerClass) -> {
			try {
				Class<? extends CacheProvider> clazz = Class.forName(providerClass).asSubclass(CacheProvider.class);
				INSTANCE.registerCacheProvider(clazz, null); // lazy, init if needed
			} catch (Exception ignored) {
			}
		};

		loadImpl.accept("io.github.iprodigy.cache.provider.androidx.AndroidLruProvider");
		loadImpl.accept("io.github.iprodigy.cache.provider.androidx.AndroidExpiringLruProvider");
		loadImpl.accept("io.github.iprodigy.cache.provider.caffeine2.Caffeine2Provider");
		loadImpl.accept("io.github.iprodigy.cache.provider.ehcache.EhcacheProvider");
		loadImpl.accept("io.github.iprodigy.cache.provider.expiringmap.ExpiringMapProvider");
		loadImpl.accept("io.github.iprodigy.cache.provider.guava.GuavaProvider");
		loadImpl.accept("io.github.iprodigy.cache.provider.infinispan.InfinispanProvider");
	}
}
