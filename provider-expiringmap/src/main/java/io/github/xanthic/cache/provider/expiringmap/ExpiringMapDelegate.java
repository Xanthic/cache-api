package io.github.xanthic.cache.provider.expiringmap;

import io.github.xanthic.cache.core.delegate.GenericMapCacheDelegate;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.jodah.expiringmap.ExpiringMap;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiFunction;

@Value
@EqualsAndHashCode(callSuper = false)
class ExpiringMapDelegate<K, V> extends GenericMapCacheDelegate<K, V> {
	ExpiringMap<K, V> map; // permits null values, so we disallow such calls in this delegate

	ExpiringMapDelegate(ExpiringMap<K, V> map) {
		super(map);
		this.map = map;
	}

	@Override
	public V put(@NotNull K key, @NotNull V value) {
		return super.put(key, Objects.requireNonNull(value));
	}

	@Override
	public V putIfAbsent(@NotNull K key, @NotNull V value) {
		return super.putIfAbsent(key, Objects.requireNonNull(value));
	}

	@Override
	public V merge(@NotNull K key, @NotNull V value, @NotNull BiFunction<V, V, V> mergeFunc) {
		return super.merge(key, Objects.requireNonNull(value), mergeFunc);
	}

	@Override
	public boolean replace(@NotNull K key, @NotNull V value) {
		return super.replace(key, Objects.requireNonNull(value));
	}

	@Override
	public boolean replace(@NotNull K key, @NotNull V oldValue, @NotNull V newValue) {
		return super.replace(key, oldValue, Objects.requireNonNull(newValue));
	}
}
