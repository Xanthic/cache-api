package io.github.iprodigy.cache;

import io.github.iprodigy.cache.providers.AndroidExpiringLruProvider;
import io.github.iprodigy.cache.providers.CaffeineProvider;
import io.github.iprodigy.cache.providers.EhcacheProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Duration;

@Tag("integration")
public class CacheApiTest {

	@Test
	public void build() {
		CacheApiSettings.getInstance().setDefaultCacheProvider(new CaffeineProvider());

		Cache<String, Integer> cache = CacheApi.<String, Integer>builder()
			.provider(new AndroidExpiringLruProvider())
			.maxSize(69L)
			.expiryTime(Duration.ofSeconds(420))
			.removalListener((key, value, cause) -> {})
			.build();
	}

	@Test
	public void ehcacheTest() {
		Cache<String, Integer> cache = CacheApi.<String, Integer>builder()
			.provider(new EhcacheProvider())
			.maxSize(32L)
			.expiryTime(Duration.ofMinutes(1))
			.removalListener((key, value, cause) -> System.out.println(key + ":" + value + "=" + cause))
			.build();
		cache.put("4/20", 420);
		Assertions.assertEquals(420, cache.get("4/20"));
		cache.clear();
		Assertions.assertNull(cache.get("4/20"));
	}

}
