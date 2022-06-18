package io.github.iprodigy.cache;

import io.github.iprodigy.cache.providers.AndroidExpiringLruProvider;
import io.github.iprodigy.cache.providers.CaffeineProvider;
import io.github.iprodigy.cache.providers.EhcacheProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Duration;

@Tag("integration")
@Slf4j
public class CacheApiTest {

	@Test
	public void build() {
		CacheApiSettings.getInstance().setDefaultCacheProvider(new CaffeineProvider());

		Cache<String, Integer> cache = CacheApi.create(spec -> {
			spec.provider(new AndroidExpiringLruProvider());
			spec.maxSize(69L);
			spec.expiryTime(Duration.ofSeconds(420));
			spec.removalListener((key, value, cause) -> {});
		});
	}

	@Test
	public void ehcacheTest() {
		Cache<String, Integer> cache = CacheApi.create(spec -> {
			spec.provider(new EhcacheProvider());
			spec.maxSize(32L);
			spec.expiryTime(Duration.ofMinutes(1));
			spec.removalListener((key, value, cause) -> log.info(key + ":" + value + "=" + cause));
		});

		cache.put("4/20", 420);
		Assertions.assertEquals(420, cache.get("4/20"));
		cache.clear();
		Assertions.assertNull(cache.get("4/20"));
	}
}
