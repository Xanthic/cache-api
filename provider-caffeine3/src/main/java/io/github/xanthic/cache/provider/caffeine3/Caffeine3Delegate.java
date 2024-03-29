package io.github.xanthic.cache.provider.caffeine3;

import com.github.benmanes.caffeine.cache.Cache;
import io.github.xanthic.cache.core.delegate.GenericMapCacheDelegate;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;

@Value
@EqualsAndHashCode(callSuper = false)
class Caffeine3Delegate<K, V> extends GenericMapCacheDelegate<K, V> {
	Cache<K, V> cache;

	public Caffeine3Delegate(com.github.benmanes.caffeine.cache.Cache<K, V> cache) {
		super(cache.asMap());
		this.cache = cache;
	}

	@Override
	public V get(@NotNull K key) {
		return cache.getIfPresent(key);
	}

	@Override
	public V computeIfAbsent(@NotNull K key, @NotNull Function<K, V> computeFunc) {
		return cache.get(key, computeFunc);
	}

	@Override
	public void clear() {
		cache.invalidateAll();
	}

	@Override
	public long size() {
		cache.cleanUp();
		return cache.estimatedSize();
	}

	@Override
	public void putAll(@NotNull Map<? extends K, ? extends V> map) {
		cache.putAll(map);
	}
}
