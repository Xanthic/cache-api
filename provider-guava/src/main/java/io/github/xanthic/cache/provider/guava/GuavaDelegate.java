package io.github.xanthic.cache.provider.guava;

import com.google.common.cache.Cache;
import io.github.xanthic.cache.core.delegate.GenericMapCacheDelegate;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
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
	public V get(@NotNull K key) {
		return cache.getIfPresent(key);
	}

	@Override
	@SneakyThrows
	public V computeIfAbsent(@NotNull K key, @NotNull Function<K, V> computeFunc) {
		return cache.get(key, () -> computeFunc.apply(key));
	}

	@Override
	public void clear() {
		cache.invalidateAll();
	}

	@Override
	public void close() {
		cache.invalidateAll();
		cache.cleanUp();
	}

	@Override
	public long size() {
		cache.cleanUp();
		return cache.size();
	}

	@Override
	public void putAll(@NotNull Map<? extends K, ? extends V> map) {
		cache.putAll(map);
	}
}
