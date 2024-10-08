package io.github.xanthic.cache.core;

import io.github.xanthic.cache.api.Cache;
import io.github.xanthic.cache.api.CacheProvider;
import io.github.xanthic.cache.api.exception.NoDefaultCacheImplementationException;
import io.github.xanthic.cache.core.provider.SimpleMapProvider;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class CacheRegistrationTest {

	@BeforeEach
	@SneakyThrows
	void beforeEachTest() {
		// reset cache settings singleton
		CacheApiSettings instance = CacheApiSettings.getInstance();

		Field providers = CacheApiSettings.class.getDeclaredField("providers");
		providers.setAccessible(true);
		((Map<?, ?>) providers.get(instance)).clear();

		Field defaultProvider = CacheApiSettings.class.getDeclaredField("defaultCacheProvider");
		defaultProvider.setAccessible(true);
		((AtomicReference<?>) defaultProvider.get(instance)).set(null);

		Method populate = CacheApiSettings.class.getDeclaredMethod("populateProviders");
		populate.setAccessible(true);
		populate.invoke(instance);
	}

	@Test
	@DisplayName("Test that NoDefaultCacheImplementationException is thrown when default cache provider is not set")
	void noDefaultCacheImplementationError() {
		Exception exception = assertThrows(NoDefaultCacheImplementationException.class, () -> {
			Cache<String, Integer> cache = CacheApi.create(spec -> {
				spec.maxSize(32L);
				spec.expiryTime(Duration.ofMinutes(1));
				spec.removalListener((key, value, cause) -> log.info(key + ":" + value + "=" + cause));
			});
		});

		Assertions.assertEquals("default cache provider is not set, no cache implementations available!", exception.getMessage());
	}

	@Test
	@DisplayName("Test that default cache provider is set on first register")
	void registerAndDefaultCacheProviderTest() {
		// register simple map provider
		CacheApiSettings.getInstance().registerCacheProvider(SimpleMapProvider.class, new SimpleMapProvider());

		// check default
		CacheProvider defaultCacheProvider = CacheApiSettings.getInstance().getDefaultCacheProvider();
		Assertions.assertEquals(SimpleMapProvider.class.getCanonicalName(), defaultCacheProvider.getClass().getCanonicalName());
	}

	@Test
	@DisplayName("Test that default cache provider can be explicitly set")
	void setDefaultCacheProviderTest() {
		// set default cache provider
		CacheApiSettings.getInstance().setDefaultCacheProvider(new SimpleMapProvider());

		// check default
		CacheProvider defaultCacheProvider = CacheApiSettings.getInstance().getDefaultCacheProvider();
		Assertions.assertEquals(SimpleMapProvider.class.getCanonicalName(), defaultCacheProvider.getClass().getCanonicalName());
	}

	@Test
	@DisplayName("Test that default cache provider is used when creating cache without explicit provider")
	void specDefaultCacheProviderTest() {
		// register simple map provider
		CacheApiSettings.getInstance().registerCacheProvider(SimpleMapProvider.class, new SimpleMapProvider());

		// init cache
		Cache<String, Integer> cache = CacheApi.create(spec -> {
			spec.maxSize(32L);
			spec.expiryTime(Duration.ofMinutes(1));
			spec.removalListener((key, value, cause) -> log.info(key + ":" + value + "=" + cause));
		});
	}

}
