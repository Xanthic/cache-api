package io.github.iprodigy.cache.provider.guava;

import com.google.common.cache.CacheBuilder;
import io.github.iprodigy.cache.api.Cache;
import io.github.iprodigy.cache.api.ICacheSpec;
import io.github.iprodigy.cache.api.domain.ExpiryType;
import io.github.iprodigy.cache.api.domain.RemovalCause;
import io.github.iprodigy.cache.core.AbstractCacheProvider;

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
}
