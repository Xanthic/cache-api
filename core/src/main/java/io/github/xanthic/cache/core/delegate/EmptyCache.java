package io.github.xanthic.cache.core.delegate;

import io.github.xanthic.cache.api.Cache;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class EmptyCache<K, V> implements Cache<K, V> {

	@SuppressWarnings("rawtypes")
	private static final Cache INSTANCE = new EmptyCache<>();

	@ApiStatus.Internal
	@SuppressWarnings("unchecked")
	public static <K, V> Cache<K, V> get() {
		return INSTANCE;
	}

	private EmptyCache() {
		// restrict instantiation
	}

	@Override
	public @Nullable V get(@NotNull K key) {
		return null;
	}

	@Override
	public @Nullable V put(@NotNull K key, @NotNull V value) {
		return null;
	}

	@Override
	public @Nullable V remove(@NotNull K key) {
		return null;
	}

	@Override
	public void clear() {
		// no-op
	}

	@Override
	public long size() {
		return 0L;
	}

	@Override
	public @Nullable V compute(@NotNull K key, @NotNull BiFunction<? super K, ? super V, ? extends V> computeFunc) {
		return computeFunc.apply(key, null);
	}

	@Override
	public V computeIfAbsent(@NotNull K key, @NotNull Function<K, V> computeFunc) {
		return computeFunc.apply(key);
	}

	@Override
	public @Nullable V computeIfPresent(@NotNull K key, @NotNull BiFunction<? super K, ? super V, ? extends V> computeFunc) {
		return null;
	}

	@Override
	public @Nullable V putIfAbsent(@NotNull K key, @NotNull V value) {
		return null;
	}

	@Override
	public V merge(@NotNull K key, @NotNull V value, @NotNull BiFunction<V, V, V> mergeFunc) {
		return value;
	}

	@Override
	public boolean replace(@NotNull K key, @NotNull V value) {
		return false;
	}

	@Override
	public boolean replace(@NotNull K key, @NotNull V oldValue, @NotNull V newValue) {
		return false;
	}

	@Override
	public @NotNull V getOrDefault(@NotNull K key, @NotNull V defaultValue) {
		return defaultValue;
	}

	@Override
	public void putAll(@NotNull Map<? extends K, ? extends V> map) {
		// no-op
	}

	@Override
	public void forEach(@NotNull BiConsumer<? super K, ? super V> action) {
		// no-op
	}
}
