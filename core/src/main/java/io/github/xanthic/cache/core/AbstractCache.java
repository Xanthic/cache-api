package io.github.xanthic.cache.core;

import io.github.xanthic.cache.api.Cache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Provides a common implementation of:
 * <ul>
 *     <li>{@link Cache#computeIfAbsent(Object, Function)}</li>
 *     <li>{@link Cache#putIfAbsent(Object, Object)}</li>
 *     <li>{@link Cache#merge(Object, Object, BiFunction)}</li>
 * </ul>
 * <p>
 * Subclasses ought to employ the same degree of locking for correctness.
 * <p>
 * Avoid this abstraction if the backing cache provider already provides an implementation for these methods.
 * <p>
 * Does not support null values.
 *
 * @param <K> The type of keys that form the cache
 * @param <V> The type of values contained in the cache
 */
public abstract class AbstractCache<K, V> implements Cache<K, V> {

	@Override
	public V computeIfAbsent(@NotNull K key, @NotNull Function<K, V> computeFunc) {
		synchronized (getLock()) {
			V old = this.get(key);
			if (old != null) return old;
			V computed = computeFunc.apply(key);
			this.put(key, computed);
			return computed;
		}
	}

	@Nullable
	@Override
	public V compute(@NotNull K key, @NotNull BiFunction<? super K, ? super V, ? extends V> computeFunc) {
		synchronized (getLock()) {
			V oldValue = this.get(key);
			V newValue = computeFunc.apply(key, oldValue);
			if (newValue != null) {
				this.put(key, newValue);
				return newValue;
			} else if (oldValue != null) {
				this.remove(key);
			}
		}
		return null;
	}

	@Override
	public V computeIfPresent(@NotNull K key, @NotNull BiFunction<? super K, ? super V, ? extends V> computeFunc) {
		synchronized (getLock()) {
			V oldValue = this.get(key);
			if (oldValue != null) {
				V newValue = computeFunc.apply(key, oldValue);
				if (newValue != null) {
					this.put(key, newValue);
					return newValue;
				} else {
					this.remove(key);
				}
			}
		}
		return null;
	}

	@Override
	public V putIfAbsent(@NotNull K key, @NotNull V value) {
		synchronized (getLock()) {
			V old = this.get(key);
			if (old == null) {
				this.put(key, value);
			}
			return old;
		}
	}

	@Override
	public V merge(@NotNull K key, @NotNull V value, @NotNull BiFunction<V, V, V> mergeFunc) {
		synchronized (getLock()) {
			V old = putIfAbsent(key, value);
			if (old == null) return value;
			V merged = mergeFunc.apply(old, value);
			this.put(key, merged);
			return merged;
		}
	}

	@Override
	public boolean replace(@NotNull K key, @NotNull V value) {
		synchronized (getLock()) {
			V old = this.get(key);
			if (old == null) return false;
			put(key, value);
			return true;
		}
	}

	@Override
	public boolean replace(@NotNull K key, @NotNull V oldValue, @NotNull V newValue) {
		// noinspection ConstantConditions
		if (oldValue == null) return false;
		synchronized (getLock()) {
			if (Objects.equals(oldValue, this.get(key))) {
				this.put(key, newValue);
				return true;
			}
		}
		return false;
	}

	@NotNull
	protected Object getLock() {
		return this;
	}

}
