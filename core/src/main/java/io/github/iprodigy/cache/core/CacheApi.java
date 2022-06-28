package io.github.iprodigy.cache.core;

import io.github.iprodigy.cache.api.Cache;

import java.util.function.Consumer;

public final class CacheApi {

	private CacheApi() {
		// prevent direct instantiation
	}

	public static <K, V> Cache<K, V> create(Consumer<CacheApiSpec<K, V>> spec) {
		CacheApiSpec<K, V> finalSpec = CacheApiSpec.process(spec);
		return finalSpec.provider().build(finalSpec);
	}

}
