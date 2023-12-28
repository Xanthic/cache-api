package io.github.xanthic.cache.springjdk17;

import io.github.xanthic.cache.springjdk17.config.CacheConfiguration;
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
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { CacheConfiguration.class })
public class SpringCacheTest {

	@Autowired
	CacheManager cacheManager;

	@Test
	@DisplayName("Tests cache get, put, putIfAbsent, retrieve, clear")
	public void putGetClearTest() {
		Cache cache = Objects.requireNonNull(cacheManager.getCache("dev"));

		// cache should be listed in cacheManager
		Assertions.assertTrue(cacheManager.getCacheNames().contains("dev"));

		// Test put/get
		cache.put("4/20", 420);
		Assertions.assertEquals(420, Objects.requireNonNull(cache.get("4/20")).get());

		// Test putIfAbsent
		Assertions.assertEquals(420, Objects.requireNonNull(cache.putIfAbsent("4/20", 69)).get());
		Assertions.assertEquals(420, Objects.requireNonNull(cache.get("4/20")).get());

		// Test retrieve
		Assertions.assertEquals(420, unwrapValue(Objects.requireNonNull(cache.retrieve("4/20")).join()));
		CompletableFuture<?> missing = cache.retrieve("8/21");
		Assertions.assertTrue(missing == null || missing.join() == null);
		cache.put("5/11", null);
		Object wrappedMissing = Objects.requireNonNull(cache.retrieve("5/11")).join();
		Assertions.assertTrue(wrappedMissing instanceof Cache.ValueWrapper);
		Assertions.assertNull(unwrapValue(wrappedMissing));
		Assertions.assertNull(Objects.requireNonNull(cache.retrieve("5/11", () -> CompletableFuture.completedFuture(1605))).join());
		Assertions.assertEquals(69, cache.retrieve("6/9", () -> CompletableFuture.supplyAsync(() -> 69)).join());
		Assertions.assertEquals(69, cache.retrieve("6/9", () -> CompletableFuture.supplyAsync(() -> 70)).join());

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

	@Test
	@DisplayName("Tests the eviction of entries based on max size")
	public void evictionTest() {
		XanthicSpringCacheManager xanthicSpringCacheManager = (XanthicSpringCacheManager) cacheManager;
		xanthicSpringCacheManager.registerCache("small-cache", spec -> {
			spec.maxSize(2L);
		});

		Cache cache = Objects.requireNonNull(cacheManager.getCache("small-cache"));
		cache.put("first", 1);
		cache.put("second", 2);
		cache.put("third", 3);

		Assertions.assertEquals(2, Objects.requireNonNull(cache.get("second")).get());
		Assertions.assertEquals(3, Objects.requireNonNull(cache.get("third")).get());
		await().atLeast(100, TimeUnit.MILLISECONDS)
			.atMost(5, TimeUnit.SECONDS)
			.until(() -> cache.get("first") == null);
	}

	@Test
	@DisplayName("Tests the eviction of entries based on max size")
	public void valueLoaderTest() {
		XanthicSpringCacheManager xanthicSpringCacheManager = (XanthicSpringCacheManager) cacheManager;
		xanthicSpringCacheManager.registerCache("value-cache", spec -> {
			spec.maxSize(100L);
		});
		Cache cache = Objects.requireNonNull(cacheManager.getCache("value-cache"));

		AtomicInteger callCounter = new AtomicInteger(0);
		Callable<String> valueLoader = () -> {
			callCounter.incrementAndGet();
			return "value-loaded";
		};

		String value = cache.get("key", valueLoader);
		Assertions.assertEquals("value-loaded", value);
		Assertions.assertEquals(1, callCounter.get(), "Value loader should only be called once");

		// Check that the valueLoader is not called again
		value = cache.get("key", valueLoader);
		Assertions.assertEquals("value-loaded", value);
		Assertions.assertEquals(1, callCounter.get(), "Value loader should still be called only once");
	}

	@Test
	@DisplayName("Tests the eviction of entries based on max size")
	public void valueLoaderConcurrentTest() throws InterruptedException {
		XanthicSpringCacheManager xanthicSpringCacheManager = (XanthicSpringCacheManager) cacheManager;
		xanthicSpringCacheManager.registerCache("value-cache-concurrent", spec -> {
			spec.maxSize(100L);
		});
		Cache cache = Objects.requireNonNull(cacheManager.getCache("value-cache-concurrent"));

		AtomicInteger callCounter = new AtomicInteger(0);
		Callable<String> valueLoader = () -> {
			callCounter.incrementAndGet();
			return "value-loaded";
		};

		int numThreads = 10;
		ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
		for (int i = 0; i < numThreads; i++) {
			executorService.submit(() -> {
				String value = cache.get("key", valueLoader);
				Assertions.assertEquals("value-loaded", value);
			});
		}
		executorService.awaitTermination(5, TimeUnit.SECONDS);

		String value = cache.get("key", valueLoader);
		Assertions.assertEquals("value-loaded", value);
		Assertions.assertEquals(1, callCounter.get(), "Value loader should only be called once");
	}

	private static Object unwrapValue(Object value) {
		return value instanceof Cache.ValueWrapper ? ((Cache.ValueWrapper) value).get() : value;
	}

}
