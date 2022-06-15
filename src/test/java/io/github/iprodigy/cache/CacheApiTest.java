package io.github.iprodigy.cache;

import io.github.iprodigy.cache.providers.AndroidLruProvider;
import io.github.iprodigy.cache.providers.CaffeineProvider;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Duration;

@Tag("integration")
public class CacheApiTest {

	@Test
	public void build() {
		CacheApiSettings.getInstance().setDefaultCacheProvider(new CaffeineProvider());

		Cache<String, Integer> cache = CacheApi.<String, Integer>builder()
			.provider(new AndroidLruProvider())
			.maxSize(69L)
			.expiryTime(Duration.ofSeconds(420))
			.removalListener((key, value, cause) -> {})
			.build();
	}

}
