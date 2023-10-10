package io.github.xanthic.cache.provider.ehcache;

import io.github.xanthic.cache.api.Cache;
import lombok.Value;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

@Value
@ApiStatus.Internal
@SuppressWarnings("unchecked")
public class EhcacheDelegate<K, V> implements Cache<K, V> {
	org.ehcache.Cache<Object, Object> cache;

	@Override
	public @Nullable V get(@NotNull K key) {
		return (V) cache.get(key);
	}

	@Override
	public @Nullable V put(@NotNull K key, @NotNull V value) {
		while (true) {
			Object prev = cache.get(key);
			if (prev == null) {
				if (cache.putIfAbsent(key, value) == null) {
					return null;
				}
			} else {
				if (cache.replace(key, prev, value)) {
					return (V) prev;
				}
			}
		}
	}

	@Override
	public @Nullable V remove(@NotNull K key) {
		while (true) {
			Object prev = cache.get(key);
			if (prev == null) {
				return null;
			}

			if (cache.remove(key, prev)) {
				return (V) prev;
			}
		}
	}

	@Override
	public void clear() {
		cache.clear();
	}

	@Override
	public long size() {
		LongAdder l = new LongAdder();
		cache.forEach(e -> l.increment());
		return l.sum();
	}

	@Override
	public @Nullable V compute(@NotNull K key, @NotNull BiFunction<? super K, ? super V, ? extends V> computeFunc) {
		while (true) {
			Object old = cache.get(key);
			V computed = computeFunc.apply(key, (V) old);
			if (computed == null) {
				if (old == null || cache.remove(key, old)) {
					return null;
				}
			} else {
				if (old == null) {
					if (cache.putIfAbsent(key, computed) == null) {
						return computed;
					}
				} else {
					if (cache.replace(key, old, computed)) {
						return computed;
					}
				}
			}
		}
	}

	@Override
	public V computeIfAbsent(@NotNull K key, @NotNull Function<K, V> computeFunc) {
		Object initial = cache.get(key);
		if (initial != null) {
			return (V) initial;
		}

		V computed = computeFunc.apply(key);
		if (computed == null) {
			return null;
		}

		Object previous = cache.putIfAbsent(key, computed);
		if (previous == null) {
			return computed;
		} else {
			return (V) previous;
		}
	}

	@Override
	public @Nullable V computeIfPresent(@NotNull K key, @NotNull BiFunction<? super K, ? super V, ? extends V> computeFunc) {
		while (true) {
			Object oldValue = cache.get(key);
			if (oldValue == null) {
				return null;
			}
			V computed = computeFunc.apply(key, (V) oldValue);
			if (computed == null) {
				if (cache.remove(key, oldValue))
					return null;
			} else {
				if (cache.replace(key, oldValue, computed)) {
					return computed;
				}
			}
		}
	}

	@Override
	public @Nullable V putIfAbsent(@NotNull K key, @NotNull V value) {
		return (V) cache.putIfAbsent(key, value);
	}

	@Override
	public V merge(@NotNull K key, @NotNull V value, @NotNull BiFunction<V, V, V> mergeFunc) {
		Object oldValue = cache.get(key);
		while (true) {
			if (oldValue == null) {
				Object latest = cache.putIfAbsent(key, value);
				if (latest == null) {
					return value;
				} else {
					oldValue = latest;
				}
			} else {
				V merged = mergeFunc.apply((V) oldValue, value);
				if (cache.replace(key, oldValue, merged)) {
					return merged;
				} else {
					oldValue = cache.get(key);
				}
			}
		}
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
		cache.forEach(e -> action.accept((K) e.getKey(), (V) e.getValue()));
	}
}
