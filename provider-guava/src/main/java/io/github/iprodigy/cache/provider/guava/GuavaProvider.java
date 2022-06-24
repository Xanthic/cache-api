package io.github.iprodigy.cache.provider.guava;

import com.google.common.cache.CacheBuilder;
import io.github.iprodigy.cache.api.Cache;
import io.github.iprodigy.cache.api.ICacheSpec;
import io.github.iprodigy.cache.api.domain.ExpiryType;
import io.github.iprodigy.cache.api.domain.RemovalCause;
import io.github.iprodigy.cache.core.AbstractCacheProvider;
import io.github.iprodigy.cache.core.delegate.GenericMapCacheDelegate;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.Value;

import java.util.function.Function;

public final class GuavaProvider extends AbstractCacheProvider {
	@Override
	public <K, V> Cache<K, V> build(ICacheSpec<K, V> spec) {
		CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
		if (spec.maxSize() != null) builder.maximumSize(spec.maxSize());
		if (spec.removalListener() != null) {
			//noinspection ConstantConditions
			builder = builder.removalListener(e -> {
				//noinspection unchecked
				spec.removalListener().onRemoval((K) e.getKey(), (V) e.getValue(), getCause(e.getCause()));
			});
		}
		CacheBuilder<Object, Object> finalBuilder = builder;
		handleExpiration(spec.expiryTime(), spec.expiryType(), (time, type) -> {
			if (type == ExpiryType.POST_WRITE)
				finalBuilder.expireAfterWrite(time);
			else
				finalBuilder.expireAfterAccess(time);
		});

		return new GuavaDelegate<>(finalBuilder.build());
	}

	@SuppressWarnings("DuplicatedCode")
	private static RemovalCause getCause(com.google.common.cache.RemovalCause cause) {
		switch (cause) {
			case EXPLICIT:
				return RemovalCause.MANUAL;
			case REPLACED:
				return RemovalCause.REPLACED;
			case EXPIRED:
				return RemovalCause.TIME;
			case SIZE:
				return RemovalCause.SIZE;
			case COLLECTED:
			default:
				return RemovalCause.OTHER;
		}
	}

	@Value
	@EqualsAndHashCode(callSuper = false)
	private static class GuavaDelegate<K, V> extends GenericMapCacheDelegate<K, V> {
		com.google.common.cache.Cache<K, V> cache;

		public GuavaDelegate(com.google.common.cache.Cache<K, V> cache) {
			super(cache.asMap());
			this.cache = cache;
		}

		@Override
		public V get(K key) {
			return cache.getIfPresent(key);
		}

		@Override
		@SneakyThrows
		public V computeIfAbsent(K key, Function<K, V> computeFunc) {
			return cache.get(key, () -> computeFunc.apply(key));
		}

		@Override
		public void clear() {
			cache.invalidateAll();
		}

		@Override
		public long size() {
			cache.cleanUp();
			return cache.size();
		}
	}
}
