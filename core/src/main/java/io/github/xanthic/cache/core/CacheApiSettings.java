package io.github.xanthic.cache.core;

import io.github.xanthic.cache.api.CacheProvider;
import io.github.xanthic.cache.api.domain.MisconfigurationPolicy;
import io.github.xanthic.cache.api.exception.NoDefaultCacheImplementationException;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Holds a registry of default settings and cache providers.
 */
@Slf4j
public final class CacheApiSettings {
	private final Map<Class<? extends CacheProvider>, CacheProvider> providers = Collections.synchronizedMap(new IdentityHashMap<>(16));
	private final AtomicReference<Class<? extends CacheProvider>> defaultCacheProvider = new AtomicReference<>();

	/**
	 * The default misconfiguration policy.
	 *
	 * @see MisconfigurationPolicy
	 */
	@Getter
	@Setter
	private volatile MisconfigurationPolicy defaultMisconfigurationPolicy = MisconfigurationPolicy.IGNORE;

	private CacheApiSettings() {
		this.populateProviders();
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
		log.debug("Xanthic: Default cache provider was set to {}.", clazz.getSimpleName());
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
			log.trace("Xanthic: Constructing lazily registered cache provider {}...", clazz.getSimpleName());
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
		if (providers.put(cacheProviderClass, cacheProvider) == null) {
			log.trace("Xanthic: Initially registered cache provider {}", cacheProviderClass.getCanonicalName());
		}

		if (defaultCacheProvider.compareAndSet(null, cacheProviderClass)) {
			log.info("Xanthic: Automatically set default cache provider to {}.", cacheProviderClass.getSimpleName());
		}
	}

	private void populateProviders() {
		log.debug("Xanthic: Registering canonical cache providers from the classpath...");

		// prepare service loader
		ServiceLoader<AbstractCacheProvider> loader;
		try {
			loader = getServiceLoader();
		} catch (ServiceConfigurationError e) {
			log.error("Failed to create CacheProvider service loader!", e);
			return;
		}

		SortedSet<AbstractCacheProvider> loaded = new TreeSet<>(
			Comparator.comparingInt(AbstractCacheProvider::getDiscoveryOrder)
				.thenComparing(provider -> provider.getClass().getName())
				.thenComparingInt(Object::hashCode)
		);

		// instantiate providers
		Iterator<AbstractCacheProvider> it = loader.iterator();
		while (it.hasNext()) {
			AbstractCacheProvider provider;
			try {
				provider = it.next();
			} catch (ServiceConfigurationError | AccessControlException e) {
				log.error("Failed to instantiate cache provider via service loader!", e);
				continue;
			}
			loaded.add(provider);
		}

		// register providers
		loaded.forEach(provider -> registerCacheProvider(provider.getClass(), provider));

		log.debug("Xanthic: Loaded {} canonical cache provider(s) on settings construction!", providers.size());
	}

	/**
	 * @return the cache api settings singleton
	 */
	@NotNull
	public static CacheApiSettings getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private static ServiceLoader<AbstractCacheProvider> getServiceLoader() {
		ClassLoader classLoader = CacheApiSettings.class.getClassLoader();

		SecurityManager securityManager = System.getSecurityManager();
		if (securityManager != null) {
			PrivilegedAction<ServiceLoader<AbstractCacheProvider>> action = () -> ServiceLoader.load(AbstractCacheProvider.class, classLoader);
			return AccessController.doPrivileged(action);
		}

		return ServiceLoader.load(AbstractCacheProvider.class, classLoader);
	}

	private static class SingletonHolder {
		private static final CacheApiSettings INSTANCE = new CacheApiSettings();
	}
}
