package io.github.xanthic.cache.spring;

import io.github.xanthic.cache.core.CacheApi;
import io.github.xanthic.cache.core.CacheApiSpec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * CacheManager implementation that lazily builds XanthicCache instances for each getCache(java.lang.String) request.
 * Also supports a 'static' mode where the set of cache names is pre-defined through cacheNames in the constructor, with no dynamic creation of further cache regions at runtime.
 * The configuration of the underlying cache can be fine-tuned through the CacheApiSpec, passed into this CacheManager in the constructor.
 */
public class XanthicSpringCacheManager implements CacheManager {

	private final Map<String, CacheWrapper> cacheMap = new ConcurrentHashMap<>();
	private final Consumer<CacheApiSpec<Object, Object>> spec;
	private final boolean dynamic;

	/**
	 * XanthicSpringCacheManager will manage all xanthic cache instances for spring.
	 *
	 * @param spec the default CacheApiSpec used to create a new cache instances
	 */
	public XanthicSpringCacheManager(Consumer<CacheApiSpec<Object, Object>> spec) {
		this.spec = spec;
		this.dynamic = true;
	}

	/**
	 * XanthicSpringCacheManager will manage all xanthic cache instances for spring.
	 *
	 * @param spec       the default CacheApiSpec used to create a new cache instances
	 * @param cacheNames If not null, the number of caches and their names will be fixed, with no creation of further cache keys at runtime.
	 */
	public XanthicSpringCacheManager(Consumer<CacheApiSpec<Object, Object>> spec, @Nullable Collection<String> cacheNames) {
		this.spec = spec;

		if (cacheNames != null) {
			this.dynamic = false;
			for (String name : cacheNames) {
				this.cacheMap.put(name, new CacheWrapper(createCache(name, this.spec)));
			}
		} else {
			this.dynamic = true;
		}
	}

	@Override
	@Nullable
	public Cache getCache(@NotNull String name) {
		// Optimistic lock-free lookup to avoid contention: https://github.com/spring-projects/spring-framework/issues/30066
		CacheWrapper optimistic = cacheMap.get(name);
		if (optimistic != null)
			return optimistic.cache();
		if (!dynamic)
			return null;

		return this.cacheMap.computeIfAbsent(name, cacheName -> new CacheWrapper(createCache(cacheName, this.spec))).cache();
	}

	@Override
	public @NotNull Collection<String> getCacheNames() {
		return Collections.unmodifiableSet(cacheMap.keySet());
	}

	@NotNull
	@Deprecated
	@VisibleForTesting
	public Set<String> getCustomCacheNames() {
		// unfortunately O(n) so this manager can be lock-free, but this operation should be rare in practice
		Set<String> names = new HashSet<>();
		cacheMap.forEach((k, v) -> {
			if (v.custom()) {
				names.add(k);
			}
		});
		return Collections.unmodifiableSet(names);
	}

	/**
	 * Register a custom xanthic cache by customizing the CacheApiSpec.
	 *
	 * @param name the name of the cache
	 * @param spec configuration for the specified cache
	 */
	public void registerCache(String name, Consumer<CacheApiSpec<Object, Object>> spec) {
		if (!this.dynamic) throw new IllegalStateException("CacheManager has a fixed set of cache keys and does not allow creation of new caches.");

		this.cacheMap.put(name, new CacheWrapper(createCache(name, spec), true));
	}

	/**
	 * Removes a named cache from this cache manager.
	 *
	 * @param name the name of the cache
	 */
	public void removeCache(String name) {
		if (!this.dynamic) throw new IllegalStateException("CacheManager has a fixed set of cache keys and does not allow removal of existing caches.");

		this.cacheMap.remove(name);
	}

	private Cache createCache(String name, Consumer<CacheApiSpec<Object, Object>> spec) {
		return new XanthicSpringCache(name, CacheApi.create(spec));
	}
}
