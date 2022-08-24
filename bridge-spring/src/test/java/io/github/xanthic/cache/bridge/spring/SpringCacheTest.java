package io.github.xanthic.cache.bridge.spring;

import io.github.xanthic.cache.bridge.spring.config.CacheConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Objects;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { CacheConfiguration.class })
public class SpringCacheTest {

	@Autowired
	CacheManager cacheManager;

	@Test
	@DisplayName("Tests cache get, put, putIfAbsent, clear")
	public void putGetClearTest() {
		Cache cache = cacheManager.getCache("dev");

		// cache should be listed in cacheManager
		Assertions.assertTrue(cacheManager.getCacheNames().contains("dev"));

		// Test put/get
		cache.put("4/20", 420);
		Assertions.assertEquals(420, Objects.requireNonNull(cache.get("4/20")).get());

		// Test putIfAbsent
		Assertions.assertEquals(420, Objects.requireNonNull(cache.putIfAbsent("4/20", 69)).get());
		Assertions.assertEquals(420, Objects.requireNonNull(cache.get("4/20")).get());

		// Test clear
		cache.clear();
		Assertions.assertNull(cache.get("4/20"));
	}

	@Test
	@DisplayName("Tests the registration and usage of a custom cache")
	public void registerCustomCacheTest() {
		XanthicSpringCacheManager xanthicSpringCacheManager = (XanthicSpringCacheManager) cacheManager;
		xanthicSpringCacheManager.registerCache("my-custom-cache", spec -> {
			spec.maxSize(1L);
		});

		// registration check
		Assertions.assertTrue(xanthicSpringCacheManager.getCustomCacheNames().contains("my-custom-cache"), "getCustomCacheNames should contain my-custom-cache");

		// cache available
		Cache cache = cacheManager.getCache("my-custom-cache");
		Assertions.assertNotNull(cache, "my-custom-cache should not be null");
	}

}
