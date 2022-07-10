package io.github.iprodigy.cache.provider.androidx;

import androidx.collection.LruCache;
import io.github.iprodigy.cache.core.AbstractCache;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
@EqualsAndHashCode(callSuper = false)
class LruDelegate<K, V> extends AbstractCache<K, V> {
	LruCache<K, V> cache;

	@Override
	public V get(K key) {
		return cache.get(key);
	}

	@Override
	public V put(K key, V value) {
		return cache.put(key, value);
	}

	@Override
	public V remove(K key) {
		return cache.remove(key);
	}

	@Override
	public void clear() {
		cache.evictAll();
	}

	@Override
	public long size() {
		return cache.size();
	}

	@NotNull
	@Override
	protected Object getLock() {
		return this.cache;
	}
}
