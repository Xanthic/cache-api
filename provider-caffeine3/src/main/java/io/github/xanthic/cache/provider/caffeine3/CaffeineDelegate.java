package io.github.xanthic.cache.provider.caffeine3;

import com.github.benmanes.caffeine.cache.Cache;
import io.github.xanthic.cache.core.delegate.GenericMapCacheDelegate;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@Value
@EqualsAndHashCode(callSuper = false)
class CaffeineDelegate<K, V> extends GenericMapCacheDelegate<K, V> {
	Cache<K, V> cache;

	public CaffeineDelegate(com.github.benmanes.caffeine.cache.Cache<K, V> cache) {
		super(cache.asMap());
		this.cache = cache;
	}

	@Override
	public V get(K key) {
		return cache.getIfPresent(key);
	}

	@Override
	public V computeIfAbsent(K key, @NotNull Function<K, V> computeFunc) {
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
}
