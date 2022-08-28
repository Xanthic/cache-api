package io.github.xanthic.cache.core;

import io.github.xanthic.cache.api.Cache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class LockedAbstractCache<K, V> implements Cache<K, V> {

	protected final ReadWriteLock lock = new ReentrantReadWriteLock();

	@Override
	public V get(@NotNull K key) {
		return read(() -> getUnlocked(key));
	}

	@Override
	public V put(@NotNull K key, @NotNull V value) {
		return write(() -> {
			V old = getUnlocked(key);
			putUnlocked(key, value);
			return old;
		});
	}

	@Override
	public V remove(@NotNull K key) {
		// Not all underlying implementations provide a remove function that yields the previous value
		// As a result, we must block reads/writes to get the previous value, before removing it
		return write(() -> {
			V value = getUnlocked(key);
			removeUnlocked(key);
			return value;
		});
	}

	@Override
	public void clear() {
		write(() -> {
			clearUnlocked();
			return Void.TYPE;
		});
	}

	@Override
	public long size() {
		return read(this::sizeUnlocked);
	}

	@Override
	public V compute(@NotNull K key, @NotNull BiFunction<? super K, ? super V, ? extends V> computeFunc) {
		return write(() -> {
			V oldValue = getUnlocked(key);
			V newValue = computeFunc.apply(key, oldValue);
			if (newValue != null) {
				putUnlocked(key, newValue);
				return newValue;
			} else if (oldValue != null) {
				removeUnlocked(key);
			}
			return null;
		});
	}

	@Override
	public V computeIfAbsent(@NotNull K key, @NotNull Function<K, V> computeFunc) {
		// Optimization: no compute needed if present
		V oldOptimistic = get(key);
		if (oldOptimistic != null) return oldOptimistic;

		return write(() -> {
			V old = getUnlocked(key);
			if (old != null) return old;
			V computed = computeFunc.apply(key);
			putUnlocked(key, computed);
			return computed;
		});
	}

	@Override
	public V computeIfPresent(@NotNull K key, @NotNull BiFunction<? super K, ? super V, ? extends V> computeFunc) {
		// Optimization: no compute needed if absent
		if (get(key) == null) return null;

		return write(() -> {
			V oldValue = getUnlocked(key);
			if (oldValue != null) {
				V newValue = computeFunc.apply(key, oldValue);
				if (newValue != null) {
					putUnlocked(key, newValue);
					return newValue;
				} else {
					removeUnlocked(key);
				}
			}
			return null;
		});
	}

	@Override
	public V putIfAbsent(@NotNull K key, @NotNull V value) {
		// Optimization: no put if present
		V oldOptimistic = get(key);
		if (oldOptimistic != null) return oldOptimistic;

		return write(() -> {
			V old = getUnlocked(key);
			if (old == null) {
				putUnlocked(key, value);
			}
			return old;
		});
	}

	@Override
	public V merge(@NotNull K key, @NotNull V value, @NotNull BiFunction<V, V, V> mergeFunc) {
		return write(() -> {
			V old = putIfAbsent(key, value); // safe due to reentrancy
			if (old == null) return value;
			V merged = mergeFunc.apply(old, value);
			putUnlocked(key, merged);
			return merged;
		});
	}

	@Override
	public boolean replace(@NotNull K key, @NotNull V value) {
		// Optimization: no replacement if absent
		if (get(key) == null) return false;

		return write(() -> {
			V old = getUnlocked(key);
			if (old == null) return false;
			putUnlocked(key, value);
			return true;
		});
	}

	@Override
	public boolean replace(@NotNull K key, @NotNull V oldValue, @NotNull V newValue) {
		//noinspection ConstantConditions
		if (oldValue == null) return false;

		// Optimization: no replacement if absent
		V oldOptimistic = get(key);
		if (oldOptimistic == null) return false;

		// Optimization: no replacement if not matching
		if (!Objects.equals(oldValue, oldOptimistic)) return false;

		return write(() -> {
			if (Objects.equals(oldValue, getUnlocked(key))) {
				putUnlocked(key, newValue);
				return true;
			}
			return false;
		});
	}

	@Override
	public void putAll(@NotNull Map<? extends K, ? extends V> map) {
		read(() -> {
			map.forEach(this::putUnlocked);
			return Void.TYPE;
		});
	}

	@Nullable
	protected abstract V getUnlocked(@NotNull K key);

	protected abstract void putUnlocked(@NotNull K key, @NotNull V value);

	protected abstract void removeUnlocked(@NotNull K key);

	protected abstract void clearUnlocked();

	protected abstract long sizeUnlocked();

	protected <T> T read(Supplier<T> reader) {
		lock.readLock().lock();
		try {
			return reader.get();
		} finally {
			lock.readLock().unlock();
		}
	}

	protected <T> T write(Supplier<T> writer) {
		lock.writeLock().lock();
		try {
			return writer.get();
		} finally {
			lock.writeLock().unlock();
		}
	}

}
