package io.github.iprodigy.cache;

import io.github.iprodigy.cache.providers.CacheProvider;
import lombok.Builder;
import org.checkerframework.checker.units.qual.K;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

public final class CacheApi {

	private CacheApi() {
		// prevent direct instantiation
	}

	public static <K, V> Cache<K, V> create(Consumer<CacheApiSpec<K, V>> spec) {
		CacheApiSpec<K, V> finalSpec = CacheApiSpec.process(spec);
		return finalSpec.provider().build(finalSpec.maxSize(), finalSpec.expiryTime(), finalSpec.expiryType(), finalSpec.removalListener(), finalSpec.executor());
	}

}
