package io.github.iprodigy.cache;

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
