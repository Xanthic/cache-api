package io.github.iprodigy.cache;

import io.github.iprodigy.cache.providers.AndroidLruProvider;
import io.github.iprodigy.cache.providers.CacheProvider;
import io.github.iprodigy.cache.providers.CaffeineProvider;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

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
		this.defaultCacheProvider.set(this.registerCacheProvider(provider));
	}

	@NotNull
	@SneakyThrows
	public CacheProvider getDefaultCacheProvider() {
		Class<? extends CacheProvider> clazz = this.defaultCacheProvider.get();
		if (clazz == null) clazz = CaffeineProvider.class;
		CacheProvider provider = providers.get(clazz);
		if (provider == null) {
			provider = clazz.newInstance();
			providers.put(clazz, provider);
		}
		return provider;
	}

	private Class<? extends CacheProvider> registerCacheProvider(@NotNull CacheProvider provider) {
		Class<? extends CacheProvider> clazz = provider.getClass();
		providers.put(clazz, provider);
		defaultCacheProvider.compareAndSet(null, clazz);
		return clazz;
	}

	public static CacheApiSettings getInstance() {
		return INSTANCE;
	}

	static {
		BiConsumer<String, Supplier<CacheProvider>> loadImpl = (backingClassName, provider) -> {
			try {
				Class.forName(backingClassName);
				INSTANCE.registerCacheProvider(provider.get());
			} catch (Exception ignored) {
			}
		};

		loadImpl.accept("androidx.collection.LruCache", AndroidLruProvider::new);
		loadImpl.accept("com.github.benmanes.caffeine.cache.Caffeine", CaffeineProvider::new);
	}
}
