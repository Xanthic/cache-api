package io.github.xanthic.cache.provider.infinispan;

import io.github.xanthic.cache.api.Cache;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

@Value
class InfinispanDelegate<K, V> implements Cache<K, V> {
	org.infinispan.Cache<K, V> cache;

	@Override
	public V get(@NotNull K key) {
		return cache.get(key);
	}

	@Override
	public V put(@NotNull K key, @NotNull V value) {
		return cache.put(key, value);
	}

	@Override
	public V remove(@NotNull K key) {
		return cache.remove(key);
	}

	@Override
	public void clear() {
		cache.clear();
	}

	@Override
	public void close() {
		cache.clear();
		cache.shutdown();
	}

	@Override
	public long size() {
		return cache.size();
	}

	@Nullable
	@Override
	public V compute(@NotNull K key, @NotNull BiFunction<? super K, ? super V, ? extends V> computeFunc) {
		return cache.compute(key, computeFunc);
	}

	@Override
	public V computeIfAbsent(@NotNull K key, @NotNull Function<K, V> computeFunc) {
		return cache.computeIfAbsent(key, computeFunc);
	}

	@Override
	public V computeIfPresent(@NotNull K key, @NotNull BiFunction<? super K, ? super V, ? extends V> computeFunc) {
		return cache.computeIfPresent(key, computeFunc);
	}

	@Override
	public V putIfAbsent(@NotNull K key, @NotNull V value) {
		return cache.putIfAbsent(key, value);
	}

	@Override
	public V merge(@NotNull K key, @NotNull V value, @NotNull BiFunction<V, V, V> mergeFunc) {
		return cache.merge(key, value, mergeFunc);
	}

	@Override
	public boolean replace(@NotNull K key, @NotNull V value) {
		return cache.replace(key, value) != null;
	}

	@Override
	public boolean replace(@NotNull K key, @NotNull V oldValue, @NotNull V newValue) {
		return cache.replace(key, oldValue, newValue);
	}

	@Override
	public void putAll(@NotNull Map<? extends K, ? extends V> map) {
		cache.putAll(map);
	}

	@Override
	public void forEach(@NotNull BiConsumer<? super K, ? super V> action) {
		cache.forEach(action);
	}
}
