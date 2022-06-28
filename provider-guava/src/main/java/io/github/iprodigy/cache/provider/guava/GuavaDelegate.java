package io.github.iprodigy.cache.provider.guava;

import com.google.common.cache.Cache;
import io.github.iprodigy.cache.core.delegate.GenericMapCacheDelegate;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.Value;

import java.util.function.Function;

@Value
@EqualsAndHashCode(callSuper = false)
class GuavaDelegate<K, V> extends GenericMapCacheDelegate<K, V> {
	Cache<K, V> cache;

	public GuavaDelegate(com.google.common.cache.Cache<K, V> cache) {
		super(cache.asMap());
		this.cache = cache;
	}

	@Override
	public V get(K key) {
		return cache.getIfPresent(key);
	}

	@Override
	@SneakyThrows
	public V computeIfAbsent(K key, Function<K, V> computeFunc) {
		return cache.get(key, () -> computeFunc.apply(key));
	}

	@Override
	public void clear() {
		cache.invalidateAll();
	}

	@Override
	public long size() {
		cache.cleanUp();
		return cache.size();
	}
}
