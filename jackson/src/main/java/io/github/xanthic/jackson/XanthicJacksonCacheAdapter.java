package io.github.xanthic.jackson;

import com.fasterxml.jackson.databind.util.LookupCache;
import io.github.xanthic.cache.api.Cache;
import io.github.xanthic.cache.core.CacheApi;
import io.github.xanthic.cache.core.CacheApiSpec;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Value
@RequiredArgsConstructor
public class XanthicJacksonCacheAdapter<K, V> implements LookupCache<K, V> {

	/**
	 * The Xanthic cache to use as a Jackson {@link LookupCache}.
	 */
	Cache<K, V> cache;

	/**
	 * The specification associated with the constructed cache.
	 */
	Consumer<CacheApiSpec<K, V>> spec;

	/**
	 * Creates a Jackson {@link LookupCache} by wrapping a Xanthic cache with this adapter.
	 *
	 * @param spec the cache specification (note: specifying {@link CacheApiSpec#maxSize(Long)} is recommended)
	 */
	public XanthicJacksonCacheAdapter(@NotNull Consumer<CacheApiSpec<K, V>> spec) {
		this(CacheApi.create(spec), spec);
	}

	@Override
	public int size() {
		return (int) cache.size();
	}

	@Override
	@SuppressWarnings("unchecked")
	public V get(Object key) {
		return cache.get((K) key);
	}

	@Override
	public V put(K key, V value) {
		return cache.put(key, value);
	}

	@Override
	public V putIfAbsent(K key, V value) {
		return cache.putIfAbsent(key, value);
	}

	@Override
	public void clear() {
		cache.clear();
	}

	// TODO: @Override
	public void contents(BiConsumer<K, V> consumer) {
		cache.forEach(consumer);
	}

	// TODO: @Override
	public XanthicJacksonCacheAdapter<K, V> emptyCopy() {
		return new XanthicJacksonCacheAdapter<>(spec);
	}
}
