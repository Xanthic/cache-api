package io.github.xanthic.cache.bridge.spring;

import io.github.xanthic.cache.core.CacheApi;
import io.github.xanthic.cache.core.CacheApiSpec;
import lombok.Getter;
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
 * Also supports a 'static' mode where the set of cache names is pre-defined through setCacheNames(java.util.Collection<java.lang.String>), with no dynamic creation of further cache regions at runtime.
 * The configuration of the underlying cache can be fine-tuned through the CacheApiSpec, passed into this CacheManager in the constructor.
 */
public class XanthicSpringCacheManager implements CacheManager {

	private final Map<String, Cache> cacheMap = new HashMap<>();
	@Getter
	private final Set<String> customCacheNames = new HashSet<>();
	private final Consumer<CacheApiSpec<Object, Object>> spec;
	private boolean dynamic = true;

	/**
	 * XanthicSpringCacheManager will manage all xanthic cache instances for spring.
	 *
	 * @param spec the default CacheApiSpec used to create a new cache instances
	 */
	public XanthicSpringCacheManager(Consumer<CacheApiSpec<Object, Object>> spec) {
		this.spec = spec;
	}

	@Override
	@Nullable
	public Cache getCache(@NotNull String name) {
		return this.cacheMap.computeIfAbsent(name, cacheName -> this.dynamic ? createCache(cacheName, this.spec) : null);
	}

	@Override
	public @NotNull Collection<String> getCacheNames() {
		return this.cacheMap.keySet();
	}

	/**
	 * Specify the set of cache names for this CacheManager's 'static' mode.
	 *
	 * <p>The number of caches and their names will be fixed after a call to this method, with no creation of further cache regions at runtime.</p>
	 * <p>Calling this with a {@code null} collection argument resets the mode to 'dynamic', allowing for further creation of caches again.</p>
	 */
	public void setCacheNames(@Nullable Collection<String> cacheNames) {
		if (cacheNames != null) {
			for (String name : cacheNames) {
				this.cacheMap.put(name, createCache(name, this.spec));
			}
			this.dynamic = false;
		} else {
			this.dynamic = true;
		}
	}

	/**
	 * Register a custom xanthic cache by customizing the CacheApiSpec.
	 *
	 * @param name the name of the cache
	 * @param spec configuration for the specified cache
	 */
	public void registerCache(String name, Consumer<CacheApiSpec<Object, Object>> spec) {
		this.cacheMap.put(name, createCache(name, spec));
		this.customCacheNames.add(name);
	}

	private Cache createCache(String name, Consumer<CacheApiSpec<Object, Object>> spec) {
		return new XanthicSpringCache(name, CacheApi.create(spec));
	}
}
