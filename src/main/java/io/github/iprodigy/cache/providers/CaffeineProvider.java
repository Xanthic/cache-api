package io.github.iprodigy.cache.providers;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;
import io.github.iprodigy.cache.Cache;
import io.github.iprodigy.cache.ExpiryType;
import io.github.iprodigy.cache.RemovalCause;
import io.github.iprodigy.cache.RemovalListener;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

public final class CaffeineProvider extends AbstractCacheProvider {

	@Override
	public <K, V> Cache<K, V> build(
		@Nullable Long maxSize,
		@Nullable Duration expiryTime,
		@Nullable ExpiryType expiryType,
		@Nullable RemovalListener<K, V> removalListener,
		@Nullable ScheduledExecutorService executor
	) {
		Caffeine<Object, Object> builder = Caffeine.newBuilder();
		if (maxSize != null) builder.maximumSize(maxSize);
		if (executor != null) builder.scheduler(Scheduler.forScheduledExecutorService(executor));
		if (removalListener != null) builder.<K, V>removalListener((key, value, cause) -> removalListener.onRemoval(key, value, getCause(cause)));
		handleExpiration(expiryTime, expiryType, (time, type) -> {
			if (type == ExpiryType.POST_WRITE)
				builder.expireAfterWrite(time);
			else
				builder.expireAfterAccess(time);
		});

		return new CaffeineDelegate<>(builder.build());
	}

	private static RemovalCause getCause(com.github.benmanes.caffeine.cache.RemovalCause cause) {
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
	private static class CaffeineDelegate<K, V> extends GenericMapCacheDelegate<K, V> {
		com.github.benmanes.caffeine.cache.Cache<K, V> cache;

		public CaffeineDelegate(com.github.benmanes.caffeine.cache.Cache<K, V> cache) {
			super(cache.asMap());
			this.cache = cache;
		}

		@Override
		public V get(K key) {
			return cache.getIfPresent(key);
		}

		@Override
		public V computeIfAbsent(K key, Function<K, V> computeFunc) {
			return cache.get(key, computeFunc);
		}

		@Override
		public void clear() {
			cache.invalidateAll();
		}

		@Override
		public long size() {
			cache.cleanUp();
			return cache.estimatedSize();
		}
	}
}
