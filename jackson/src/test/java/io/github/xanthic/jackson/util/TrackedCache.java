package io.github.xanthic.jackson.util;

import io.github.xanthic.cache.api.Cache;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

@Value
public class TrackedCache<K, V> implements Cache<K, V> {
	Cache<K, V> underlyingCache;
	AtomicBoolean interacted = new AtomicBoolean();

	public boolean hasInteraction() {
		return interacted.get();
	}

	@Override
	public @Nullable V get(@NotNull K key) {
		interacted.set(true);
		return underlyingCache.get(key);
	}

	@Override
	public @Nullable V put(@NotNull K key, @NotNull V value) {
		interacted.set(true);
		return underlyingCache.put(key, value);
	}

	@Override
	public @Nullable V remove(@NotNull K key) {
		interacted.set(true);
		return underlyingCache.remove(key);
	}

	@Override
	public void clear() {
		interacted.set(true);
		underlyingCache.clear();
	}

	@Override
	public long size() {
		interacted.set(true);
		return underlyingCache.size();
	}

	@Override
	public @Nullable V compute(@NotNull K key, @NotNull BiFunction<? super K, ? super V, ? extends V> computeFunc) {
		interacted.set(true);
		return underlyingCache.compute(key, computeFunc);
	}

	@Override
	public V computeIfAbsent(@NotNull K key, @NotNull Function<K, V> computeFunc) {
		interacted.set(true);
		return underlyingCache.computeIfAbsent(key, computeFunc);
	}

	@Override
	public @Nullable V computeIfPresent(@NotNull K key, @NotNull BiFunction<? super K, ? super V, ? extends V> computeFunc) {
		interacted.set(true);
		return underlyingCache.computeIfPresent(key, computeFunc);
	}

	@Override
	public @Nullable V putIfAbsent(@NotNull K key, @NotNull V value) {
		interacted.set(true);
		return underlyingCache.putIfAbsent(key, value);
	}

	@Override
	public V merge(@NotNull K key, @NotNull V value, @NotNull BiFunction<V, V, V> mergeFunc) {
		interacted.set(true);
		return underlyingCache.merge(key, value, mergeFunc);
	}

	@Override
	public boolean replace(@NotNull K key, @NotNull V value) {
		interacted.set(true);
		return underlyingCache.replace(key, value);
	}

	@Override
	public boolean replace(@NotNull K key, @NotNull V oldValue, @NotNull V newValue) {
		interacted.set(true);
		return underlyingCache.replace(key, oldValue, newValue);
	}

	@Override
	public @NotNull V getOrDefault(@NotNull K key, @NotNull V defaultValue) {
		interacted.set(true);
		return underlyingCache.getOrDefault(key, defaultValue);
	}

	@Override
	public void putAll(@NotNull Map<? extends K, ? extends V> map) {
		interacted.set(true);
		underlyingCache.putAll(map);
	}

	@Override
	public void forEach(@NotNull BiConsumer<? super K, ? super V> action) {
		interacted.set(true);
		underlyingCache.forEach(action);
	}
}
