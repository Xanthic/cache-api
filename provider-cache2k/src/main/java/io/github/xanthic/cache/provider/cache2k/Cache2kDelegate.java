package io.github.xanthic.cache.provider.cache2k;

import io.github.xanthic.cache.core.delegate.GenericMapCacheDelegate;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.cache2k.Cache;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Value
@EqualsAndHashCode(callSuper = false)
class Cache2kDelegate<K, V> extends GenericMapCacheDelegate<K, V> {
	Cache<K, V> cache;

	public Cache2kDelegate(Cache<K, V> cache) {
		super(cache.asMap());
		this.cache = cache;
	}

	@Override
	public V get(@NotNull K key) {
		return cache.get(key);
	}

	@Override
	public V put(@NotNull K key, @NotNull V value) {
		return cache.peekAndPut(key, value);
	}

	@Override
	public V remove(@NotNull K key) {
		return cache.peekAndRemove(key);
	}

	@Override
	public void clear() {
		cache.clear();
	}

	@Override
	public void close() {
		super.close();
		cache.close();
	}

	@Override
	public V computeIfAbsent(@NotNull K key, @NotNull Function<K, V> computeFunc) {
		return cache.computeIfAbsent(key, computeFunc);
	}

	@Override
	public boolean replace(@NotNull K key, @NotNull V value) {
		return cache.replace(key, value);
	}

	@Override
	public boolean replace(@NotNull K key, @NotNull V oldValue, @NotNull V newValue) {
		return cache.replaceIfEquals(key, oldValue, newValue);
	}

	@Override
	public void putAll(@NotNull Map<? extends K, ? extends V> map) {
		cache.putAll(map);
	}

	@Override
	public void forEach(@NotNull BiConsumer<? super K, ? super V> action) {
		cache.entries().forEach(e -> action.accept(e.getKey(), e.getValue()));
	}
}
