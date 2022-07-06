package io.github.iprodigy.cache.core.provider;

import io.github.iprodigy.cache.api.Cache;
import io.github.iprodigy.cache.api.CacheProvider;
import io.github.iprodigy.cache.core.CacheApi;
import io.github.iprodigy.cache.core.CacheApiSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
public abstract class ProviderTestBase {

	protected final CacheProvider provider;

	@Test
	public void putGetClearTest() {
		// Build cache
		Cache<String, Integer> cache = CacheApi.create(spec -> {
			spec.provider(provider);
			spec.maxSize(32L);
			spec.expiryTime(Duration.ofMinutes(1));
			spec.removalListener((key, value, cause) -> log.info(key + ":" + value + "=" + cause));
		});

		// Test put/get
		Assertions.assertNull(cache.put("4/20", 420));
		Assertions.assertEquals(420, cache.get("4/20"));

		// Test putIfAbsent
		Assertions.assertEquals(420, cache.putIfAbsent("4/20", 69));
		Assertions.assertEquals(420, cache.get("4/20"));

		// Test clear
		cache.clear();
		Assertions.assertNull(cache.get("4/20"));
	}

	@Test
	public void registeredAsDefaultTest() {
		if (!(provider instanceof SimpleMapProvider)) {
			Assertions.assertEquals(provider.getClass(), CacheApiSettings.getInstance().getDefaultCacheProvider().getClass());
		}
	}

}
