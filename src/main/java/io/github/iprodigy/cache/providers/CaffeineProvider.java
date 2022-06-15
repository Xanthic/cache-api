package io.github.iprodigy.cache.providers;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;
import io.github.iprodigy.cache.Cache;
import io.github.iprodigy.cache.CacheApiSettings;
import io.github.iprodigy.cache.ExpiryType;
import io.github.iprodigy.cache.MisconfigurationPolicy;
import io.github.iprodigy.cache.MisconfiguredCacheException;
import io.github.iprodigy.cache.RemovalCause;
import io.github.iprodigy.cache.RemovalListener;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

public final class CaffeineProvider implements CacheProvider {

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
		if (expiryTime != null) {
			ExpiryType type = expiryType != null ? expiryType : CacheApiSettings.getInstance().getDefaultExpiryType();
			if (type == ExpiryType.POST_WRITE)
				builder.expireAfterWrite(expiryTime);
			else if (type == ExpiryType.POST_ACCESS || CacheApiSettings.getInstance().getDefaultMisconfigurationPolicy() != MisconfigurationPolicy.REJECT)
				builder.expireAfterAccess(expiryTime);
			else
				throw new MisconfiguredCacheException("Expiry time was set without an expiry type specified, even as a default");
		}
		if (executor != null) builder.scheduler(Scheduler.forScheduledExecutorService(executor));
		if (removalListener != null) builder.<K, V>removalListener((key, value, cause) -> removalListener.onRemoval(key, value, getCause(cause)));

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
