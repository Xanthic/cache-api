package io.github.iprodigy.cache.providers;

import com.google.common.cache.CacheBuilder;
import io.github.iprodigy.cache.Cache;
import io.github.iprodigy.cache.ExpiryType;
import io.github.iprodigy.cache.RemovalCause;
import io.github.iprodigy.cache.RemovalListener;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

public final class GuavaProvider extends AbstractCacheProvider {
	@Override
	public <K, V> Cache<K, V> build(
		@Nullable Long maxSize,
		@Nullable Duration expiryTime,
		@Nullable ExpiryType expiryType,
		@Nullable RemovalListener<K, V> removalListener,
		@Nullable ScheduledExecutorService executor
	) {
		CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
		if (maxSize != null) builder.maximumSize(maxSize);
		if (removalListener != null) {
			//noinspection ConstantConditions
			builder = builder.removalListener(e -> {
				//noinspection unchecked
				removalListener.onRemoval((K) e.getKey(), (V) e.getCause(), getCause(e.getCause()));
			});
		}
		CacheBuilder<Object, Object> finalBuilder = builder;
		handleExpiration(expiryTime, expiryType, (time, type) -> {
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
