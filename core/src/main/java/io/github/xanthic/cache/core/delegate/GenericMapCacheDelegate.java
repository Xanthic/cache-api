package io.github.xanthic.cache.core.delegate;

import io.github.xanthic.cache.api.Cache;
import io.github.xanthic.cache.api.CacheProvider;
import io.github.xanthic.cache.api.ICacheSpec;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Delegates cache calls to a {@link Map} view that already conforms to a desired {@link ICacheSpec}.
 * <p>
 * This class should only be used when implementing a {@link CacheProvider}, which yields a {@link Map} view.
 *
 * @param <K> The type of keys that form the cache
 * @param <V> The type of values contained in the cache
 */
@Data
public class GenericMapCacheDelegate<K, V> implements Cache<K, V> {
	private final Map<K, V> map;

	@Override
	public V get(@NotNull K key) {
		return map.get(key);
	}

	@Override
	public V put(@NotNull K key, @NotNull V value) {
		return map.put(key, value);
	}

	@Nullable
	@Override
	public V compute(@NotNull K key, @NotNull BiFunction<? super K, ? super V, ? extends V> computeFunc) {
		return map.compute(key, computeFunc);
	}

	@Override
	public V computeIfAbsent(@NotNull K key, @NotNull Function<K, V> computeFunc) {
		return map.computeIfAbsent(key, computeFunc);
	}

	@Override
	public V computeIfPresent(@NotNull K key, @NotNull BiFunction<? super K, ? super V, ? extends V> computeFunc) {
		return map.computeIfPresent(key, computeFunc);
	}

	@Override
	public V remove(@NotNull K key) {
		return map.remove(key);
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public long size() {
		return map.size();
	}

	@Override
	public V putIfAbsent(@NotNull K key, @NotNull V value) {
		return map.putIfAbsent(key, value);
	}

	@Override
	public V merge(@NotNull K key, @NotNull V value, @NotNull BiFunction<V, V, V> mergeFunc) {
		return map.merge(key, value, mergeFunc);
	}

	@Override
	public boolean replace(@NotNull K key, @NotNull V value) {
		return map.replace(key, value) != null;
	}

	@Override
	public boolean replace(@NotNull K key, @NotNull V oldValue, @NotNull V newValue) {
		return map.replace(key, oldValue, newValue);
	}

	@Override
	public void putAll(@NotNull Map<? extends K, ? extends V> map) {
		this.map.putAll(map);
	}

	@Override
	public void forEach(@NotNull BiConsumer<? super K, ? super V> action) {
		this.map.forEach(action);
	}
}
