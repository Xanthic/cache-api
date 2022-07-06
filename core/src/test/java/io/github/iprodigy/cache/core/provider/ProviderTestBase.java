package io.github.iprodigy.cache.core.provider;

import io.github.iprodigy.cache.api.Cache;
import io.github.iprodigy.cache.api.CacheProvider;
import io.github.iprodigy.cache.api.domain.ExpiryType;
import io.github.iprodigy.cache.api.domain.RemovalCause;
import io.github.iprodigy.cache.core.CacheApi;
import io.github.iprodigy.cache.core.CacheApiSettings;
import io.github.iprodigy.cache.core.CacheApiSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.awaitility.Awaitility.await;

@Slf4j
@RequiredArgsConstructor
public abstract class ProviderTestBase {

	protected final CacheProvider provider;

	@Test
	@DisplayName("Tests cache get, put, putIfAbsent, and clear")
	public void putGetClearTest() {
		// Build cache
		Cache<String, Integer> cache = build(null);

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
	@DisplayName("Tests cache computeIfAbsent, merge, and remove")
	public void computeMergeRemoveTest() {
		// Build cache
		Cache<String, Integer> cache = build(null);

		// Test computeIfAbsent
		Assertions.assertEquals(420, cache.computeIfAbsent("4/20", k -> 420));
		Assertions.assertEquals(420, cache.get("4/20"));

		// Test merge
		Assertions.assertEquals(420 + 69, cache.merge("4/20", 69, Integer::sum));

		// Test remove
		Assertions.assertEquals(420 + 69, cache.remove("4/20"));
		Assertions.assertNull(cache.get("4/20"));
	}

	@Test
	@DisplayName("Test that cache size constraint is respected")
	public void sizeEvictionTest() {
		// Build cache
		Cache<String, Integer> cache = build(spec -> spec.maxSize(4L).expiryType(ExpiryType.POST_WRITE));

		// Add entries
		for (int i = 0; i < 5; i++) {
			cache.put(String.valueOf(i), i);
		}

		// Ensure eldest entry was removed
		await().atMost(30, TimeUnit.SECONDS).until(() -> cache.get("0") == null);

		// Ensure other entries are present
		for (int i = 1; i < 5; i++) {
			Assertions.assertEquals(i, cache.get(String.valueOf(i)));
		}
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
	}

	@Test
	@DisplayName("Test that cache time constraint is respected")
	public void timeEvictionTest() {
		final long expiry = 2500L;

		// Build cache
		Cache<String, Integer> cache = build(spec -> {
			spec.expiryTime(Duration.ofMillis(expiry));
			spec.expiryType(ExpiryType.POST_WRITE);
		});

		// Populate cache
		for (int i = 0; i < 16; i++) {
			cache.put(String.valueOf(i), i);
		}

		// Ensure entries are removed over time
		await().atLeast(expiry, TimeUnit.MILLISECONDS)
			.atMost(expiry * 10, TimeUnit.SECONDS)
			.until(() -> cache.size() == 0);
	}

	@Test
	@DisplayName("Tests whether the provider has been set as the default")
	public void registeredAsDefaultTest() {
		Assertions.assertEquals(provider.getClass(), CacheApiSettings.getInstance().getDefaultCacheProvider().getClass());
	}

	protected <K, V> Cache<K, V> build(Consumer<CacheApiSpec<K, V>> additionalSpec) {
		Consumer<CacheApiSpec<K, V>> baseSpec = spec -> {
			spec.provider(provider);
			spec.maxSize(32L);
			spec.expiryTime(Duration.ofMinutes(1));
			spec.removalListener((key, value, cause) -> log.info(key + ":" + value + "=" + cause));
		};
		Consumer<CacheApiSpec<K, V>> spec = additionalSpec == null ? baseSpec : baseSpec.andThen(additionalSpec);
		return CacheApi.create(spec);
	}

}
