package io.github.xanthic.cache.provider.androidx;

import androidx.collection.LruCache;
import io.github.xanthic.cache.core.AbstractCache;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

@Value
@Getter(AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false)
class LruDelegate<K, V> extends AbstractCache<K, V> {
	LruCache<K, V> cache;

	@Override
	public V get(@NotNull K key) {
		// note: underlying operations are synchronized on same object
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
		cache.evictAll();
	}

	@Override
	public long size() {
		return cache.size();
	}

	@Override
	public void forEach(@NotNull BiConsumer<? super K, ? super V> action) {
		cache.snapshot().forEach(action);
	}

	@NotNull
	@Override
	protected Object getLock() {
		return this.cache;
	}
}
