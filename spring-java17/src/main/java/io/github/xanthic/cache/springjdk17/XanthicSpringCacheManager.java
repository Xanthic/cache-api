package io.github.xanthic.cache.springjdk17;

import io.github.xanthic.cache.core.CacheApi;
import io.github.xanthic.cache.core.CacheApiSpec;
import lombok.Locked;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * CacheManager implementation that lazily builds XanthicCache instances for each getCache(java.lang.String) request.
 * Also supports a 'static' mode where the set of cache names is pre-defined through cacheNames in the constructor, with no dynamic creation of further cache regions at runtime.
 * The configuration of the underlying cache can be fine-tuned through the CacheApiSpec, passed into this CacheManager in the constructor.
 */
public class XanthicSpringCacheManager implements CacheManager {

	private final Map<String, Cache> cacheMap = new HashMap<>();
	private final Set<String> customCacheNames = new HashSet<>();
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
				this.cacheMap.put(name, createCache(name, this.spec));
			}
		} else {
			this.dynamic = true;
		}
	}

	@Override
	@Nullable
	public Cache getCache(@NotNull String name) {
		Cache optimistic = get(name);
		if (optimistic != null || !dynamic)
			return optimistic;

		return compute(name);
	}

	@Override
	@Locked.Read
	public @NotNull Collection<String> getCacheNames() {
		return Set.copyOf(cacheMap.keySet());
	}

	@Locked.Read
	public Set<String> getCustomCacheNames() {
		return Set.copyOf(customCacheNames);
	}

	/**
	 * Register a custom xanthic cache by customizing the CacheApiSpec.
	 *
	 * @param name the name of the cache
	 * @param spec configuration for the specified cache
	 */
	@Locked.Write
	public void registerCache(String name, Consumer<CacheApiSpec<Object, Object>> spec) {
		if (!this.dynamic) throw new IllegalStateException("CacheManager has a fixed set of cache keys and does not allow creation of new caches.");

		this.cacheMap.put(name, createCache(name, spec));
		this.customCacheNames.add(name);
	}

	/**
	 * Removes a named cache from this cache manager.
	 *
	 * @param name the name of the cache
	 */
	@Locked.Write
	public void removeCache(String name) {
		this.customCacheNames.remove(name);
		this.cacheMap.remove(name);
	}

	@Locked.Read
	private Cache get(String name) {
		return cacheMap.get(name);
	}

	@Locked.Write
	private Cache compute(String name) {
		return this.cacheMap.computeIfAbsent(name, cacheName -> createCache(cacheName, this.spec));
	}

	private Cache createCache(String name, Consumer<CacheApiSpec<Object, Object>> spec) {
		return new XanthicSpringCache(name, CacheApi.create(spec));
	}
}
