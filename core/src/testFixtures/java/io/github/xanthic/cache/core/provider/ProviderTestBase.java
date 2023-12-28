package io.github.xanthic.cache.core.provider;

import io.github.xanthic.cache.api.Cache;
import io.github.xanthic.cache.api.CacheProvider;
import io.github.xanthic.cache.api.domain.ExpiryType;
import io.github.xanthic.cache.api.domain.RemovalCause;
import io.github.xanthic.cache.core.CacheApi;
import io.github.xanthic.cache.core.CacheApiSettings;
import io.github.xanthic.cache.core.CacheApiSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.awaitility.Awaitility.await;

@Slf4j
@RequiredArgsConstructor
public abstract class ProviderTestBase {

	protected final CacheProvider provider;

	@Test
	@DisplayName("Tests cache get, getOrDefault, put, putIfAbsent, clear, and putAll")
	public void putGetClearTest() {
		// Build cache
		Cache<String, Integer> cache = build(null);

		// Test put/get
		Assertions.assertNull(cache.put("4/20", 420));
		Assertions.assertEquals(420, cache.get("4/20"));

		// Test getOrDefault
		Assertions.assertEquals(420, cache.getOrDefault("4/20", 1312));
		Assertions.assertEquals(314, cache.getOrDefault("pi", 314));
		Assertions.assertNull(cache.get("pi"));

		// Test putIfAbsent
		Assertions.assertNull(cache.putIfAbsent("oink", 1312));
		Assertions.assertEquals(1312, cache.get("oink"));
		Assertions.assertEquals(420, cache.putIfAbsent("4/20", 69));
		Assertions.assertEquals(420, cache.get("4/20"));

		// Test clear
		cache.clear();
		Assertions.assertNull(cache.get("4/20"));

		// Test putAll
		Map<String, Integer> m = new HashMap<>();
		for (int i = 0; i < 4; i++) {
			m.put(String.valueOf(i), i);
		}
		cache.putAll(m);
		for (int i = 0; i < 4; i++) {
			Assertions.assertEquals(i, m.get(String.valueOf(i)));
		}

		cache.close();
	}

	@Test
	@DisplayName("Tests cache compute, computeIfAbsent, computeIfPresent, merge, and remove")
	public void computeMergeRemoveTest() {
		// Build cache
		Cache<String, Integer> cache = build(null);

		// Test computeIfAbsent
		Assertions.assertEquals(420, cache.computeIfAbsent("4/20", k -> 420));
		Assertions.assertEquals(420, cache.get("4/20"));

		// Test merge
		Assertions.assertEquals(420 + 69, cache.merge("4/20", 69, Integer::sum));

		// Test computeIfPresent
		Assertions.assertNull(cache.computeIfPresent("", (k, v) -> 0));
		Assertions.assertNull(cache.put("", 0));
		Assertions.assertNull(cache.computeIfPresent("", (k, v) -> null));
		Assertions.assertNull(cache.get(""));

		Assertions.assertEquals(420 + 69 + 1, cache.computeIfPresent("4/20", (k, v) -> v + 1));

		// Test remove
		Assertions.assertEquals(420 + 70, cache.remove("4/20"));
		Assertions.assertNull(cache.get("4/20"));

		// Test compute
		Assertions.assertNull(cache.compute("a", (k, v) -> null));
		Assertions.assertNull(cache.get("a"));

		Assertions.assertEquals(9, cache.compute("a", (k, v) -> 9));
		Assertions.assertEquals(9, cache.get("a"));

		Assertions.assertEquals(10, cache.compute("a", (k, v) -> v + 1));
		Assertions.assertEquals(10, cache.get("a"));

		Assertions.assertNull(cache.compute("a", (k, v) -> null));
		Assertions.assertNull(cache.get("a"));

		cache.close();
	}

	@Test
	@DisplayName("Tests cache replace entry")
	public void replaceTest() {
		// Build cache
		Cache<String, Integer> cache = build(null);

		// Ensure no replacements occur when cache is empty
		Assertions.assertFalse(cache.replace("123", 456));
		Assertions.assertFalse(cache.replace("123", 456, 789));
		Assertions.assertNull(cache.get("123"));

		// Populate cache
		for (int i = 0; i < 4; i++) {
			cache.put(String.valueOf(i), i);
		}

		// Test replace
		Assertions.assertTrue(cache.replace("1", -1));
		Assertions.assertEquals(-1, cache.get("1"));

		Assertions.assertTrue(cache.replace("2", 2, -2));
		Assertions.assertEquals(-2, cache.get("2"));

		Assertions.assertFalse(cache.replace("3", 2, -3));
		Assertions.assertFalse(cache.replace("3", -2, -3));
		Assertions.assertEquals(3, cache.get("3"));

		Assertions.assertFalse(cache.replace("9", -9));
		Assertions.assertNull(cache.get("9"));

		cache.close();
	}

	@Test
	@DisplayName("Tests cache forEach")
	public void iterateTest() {
		// Build cache
		Cache<String, Integer> cache = build(null);

		// Add entries
		for (int i = 0; i < 3; i++) {
			cache.put(String.valueOf(i), i);
		}

		// Save output of forEach
		Map<String, Integer> observed = new HashMap<>();
		cache.forEach(observed::put);

		// Test that observed contents match expected
		Map<String, Integer> expected = new HashMap<>();
		expected.put("0", 0);
		expected.put("1", 1);
		expected.put("2", 2);

		Assertions.assertEquals(expected, observed);
		cache.close();
	}

	@Test
	@DisplayName("Test that caches with zero maximum size remain empty")
	public void zeroMaxSizeTest() {
		Cache<String, Integer> cache = build(spec -> spec.maxSize(0L));
		cache.put("1", 1);
		Assertions.assertNull(cache.get("1"));
		Assertions.assertEquals(0, cache.size());
		cache.close();
	}

	@Test
	@DisplayName("Test that caches with zero time-to-live for entries remain empty")
	public void zeroExpiryTimeTest() {
		Cache<String, Integer> cache = build(spec -> spec.expiryTime(Duration.ZERO));
		cache.put("1", 1);
		Assertions.assertNull(cache.get("1"));
		Assertions.assertEquals(0, cache.size());
		cache.close();
	}

	@Test
	@DisplayName("Test that cache size constraint is respected")
	public void sizeEvictionTest() {
		// Build cache
		Cache<String, Integer> cache = build(spec -> spec.maxSize(4L).expiryTime(null));

		// Add entries
		for (int i = 0; i < 5; i++) {
			cache.put(String.valueOf(i), i);

			// Hint to LRU/LFU impls like ehcache that 0 should be selected for eviction
			for (int j = 1; j < i; j++) {
				cache.get(String.valueOf(i));
			}
		}

		// Ensure the eldest entry was removed
		await().atMost(30, TimeUnit.SECONDS).until(() -> cache.get("0") == null);

		// Ensure other entries are present
		for (int i = 1; i < 5; i++) {
			Assertions.assertEquals(i, cache.get(String.valueOf(i)));
		}

		cache.close();
	}

	@Test
	@DisplayName("Test that removal listener is called on size-based eviction")
	public void sizeEvictionListenerTest() {
		// Track size-related evictions
		final int capacity = 16;
		final int expectedEvictions = 8;
		final int additions = capacity + expectedEvictions;
		final AtomicInteger removals = new AtomicInteger();

		// Build cache
		Cache<String, Integer> cache = build(spec -> {
			spec.maxSize((long) capacity);
			spec.removalListener((key, value, cause) -> {
				if (cause == RemovalCause.SIZE || cause == RemovalCause.OTHER)
					removals.incrementAndGet();
			});
		});

		// Add entries
		for (int i = 0; i < additions; i++) {
			cache.put(String.valueOf(i), i);
		}

		// Ensure listener is called the appropriate number of times
		await().atMost(30, TimeUnit.SECONDS).until(() -> removals.get() == expectedEvictions);
		cache.close();
	}

	@Test
	@DisplayName("Test that cache time constraint is respected")
	public void timeEvictionTest() {
		final long expiry = 1000L;

		// Build cache
		Cache<String, Integer> cache = build(spec -> {
			spec.expiryTime(Duration.ofMillis(expiry));
			spec.expiryType(ExpiryType.POST_WRITE);
			spec.executor(Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors()));
		});

		// Populate cache
		for (int i = 0; i < 16; i++) {
			cache.put(String.valueOf(i), i);
		}

		// Ensure entries are removed over time
		await().atLeast(expiry * 3 / 4, TimeUnit.MILLISECONDS)
			.atMost(90, TimeUnit.SECONDS)
			.until(() -> cache.size() == 0);

		cache.close();
	}

	@Test
	@DisplayName("Test that removal listener is called after time-based eviction")
	public void timeEvictionListenerTest() {
		final long expiry = 1000L;
		final int n = 16;
		final AtomicInteger evictions = new AtomicInteger();

		// Build cache
		Cache<String, Integer> cache = build(spec -> {
			spec.maxSize(n * 2L);
			spec.expiryTime(Duration.ofMillis(expiry));
			spec.expiryType(ExpiryType.POST_WRITE);
			spec.executor(Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors()));
			spec.removalListener((key, value, cause) -> {
				if (cause == RemovalCause.TIME || cause == RemovalCause.OTHER)
					evictions.incrementAndGet();
			});
		});

		// Populate cache
		for (int i = 0; i < n; i++) {
			cache.put(String.valueOf(i), i);
		}

		// Ensure listener is called the appropriate number of times
		await().atLeast(expiry * 3 / 4, TimeUnit.MILLISECONDS)
			.atMost(90, TimeUnit.SECONDS)
			.until(() -> evictions.get() == n);

		cache.close();
	}

	@Test
	@DisplayName("Test that removal listener is called after value replacements")
	public void replacedListenerTest() {
		final int n = 4;
		AtomicInteger replacements = new AtomicInteger();

		// Build cache
		Cache<String, Integer> cache = build(spec -> {
			spec.expiryTime(null);
			spec.maxSize(n * 2L);
			spec.removalListener((key, value, cause) -> {
				if (cause == RemovalCause.REPLACED && value < n)
					replacements.incrementAndGet();
			});
		});

		// Populate cache
		for (int i = 0; i < n; i++) {
			cache.put(String.valueOf(i), i);
		}

		// Perform replacements
		for (int i = 0; i < n; i++) {
			cache.put(String.valueOf(i), i + 100);
		}

		// Ensure listener was called the appropriate number of times
		await().atMost(30, TimeUnit.SECONDS).until(() -> replacements.get() == n);
		cache.close();
	}

	@Test
	@DisplayName("Test that removal listener is called after manual removals")
	public void manualRemovalListenerTest() {
		final int n = 4;
		AtomicInteger removals = new AtomicInteger();

		// Build cache
		Cache<String, Integer> cache = build(spec -> {
			spec.expiryTime(null);
			spec.maxSize(n * 2L);
			spec.removalListener((key, value, cause) -> {
				if (cause == RemovalCause.MANUAL)
					removals.incrementAndGet();
			});
		});

		// Populate cache
		for (int i = 0; i < n; i++) {
			cache.put(String.valueOf(i), i);
		}

		// Perform removals
		for (int i = 0; i < n; i++) {
			cache.remove(String.valueOf(i));
		}

		// Ensure listener was called the appropriate number of times
		await().atMost(90, TimeUnit.SECONDS).until(() -> removals.get() == n);
		cache.close();
	}

	@Test
	@DisplayName("Tests whether the provider has been set as the default")
	public void registeredAsDefaultTest() {
		Assertions.assertEquals(provider.getClass(), CacheApiSettings.getInstance().getDefaultCacheProvider().getClass());
	}

	@Test
	@DisplayName("Test whether cache can be built with contention flag and custom executor")
	public void buildTest() {
		build(spec -> spec.highContention(true).maxSize(null)).close();
		build(spec -> spec.highContention(true).executor(Executors.newSingleThreadScheduledExecutor())).close();
	}

	protected <K, V> Cache<K, V> build(Consumer<CacheApiSpec<K, V>> additionalSpec) {
		Consumer<CacheApiSpec<K, V>> baseSpec = spec -> {
			spec.provider(provider);
			spec.maxSize(32L);
			spec.expiryTime(Duration.ofMinutes(1L));
			spec.removalListener((key, value, cause) -> log.info(key + ":" + value + "=" + cause));
		};
		Consumer<CacheApiSpec<K, V>> spec = additionalSpec == null ? baseSpec : baseSpec.andThen(additionalSpec);
		return CacheApi.create(spec);
	}

}
