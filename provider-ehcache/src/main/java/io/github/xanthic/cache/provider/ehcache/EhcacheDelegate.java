package io.github.xanthic.cache.provider.ehcache;

import io.github.xanthic.cache.core.AbstractCache;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Value
@EqualsAndHashCode(callSuper = false)
@SuppressWarnings("unchecked")
class EhcacheDelegate<K, V> extends AbstractCache<K, V> {
	org.ehcache.Cache<Object, Object> cache;

	@Override
	public V get(@NotNull K key) {
		return (V) cache.get(key);
	}

	@Override
	public V put(@NotNull K key, @NotNull V value) {
		synchronized (getLock()) {
			V old = this.get(key);
			cache.put(key, value);
			return old;
		}
	}

	@Override
	public V remove(@NotNull K key) {
		synchronized (getLock()) {
			V old = this.get(key);
			cache.remove(key);
			return old;
		}
	}

	@Override
	public void clear() {
		cache.clear();
	}

	@Override
	public long size() {
		long n = 0;
		for (org.ehcache.Cache.Entry<Object, Object> ignored : cache) {
			n++;
		}
		return n;
	}

	@Override
	public V putIfAbsent(@NotNull K key, @NotNull V value) {
		return (V) cache.putIfAbsent(key, value);
	}

	@Override
	public void putAll(@NotNull Map<? extends K, ? extends V> map) {
		cache.putAll(map);
	}

	@Override
	public boolean replace(@NotNull K key, @NotNull V value) {
		return cache.replace(key, value) != null;
	}

	@Override
	public boolean replace(@NotNull K key, @NotNull V oldValue, @NotNull V newValue) {
		return cache.replace(key, oldValue, newValue);
	}

	@NotNull
	@Override
	protected Object getLock() {
		return this.cache;
	}
}
