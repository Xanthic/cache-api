package io.github.iprodigy.cache.core;

import io.github.iprodigy.cache.api.Cache;
import io.github.iprodigy.cache.api.CacheProvider;
import io.github.iprodigy.cache.api.exception.NoDefaultCacheImplementationException;
import io.github.iprodigy.cache.core.provider.SimpleMapProvider;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class CacheRegistrationTest {

    @BeforeEach
    @SneakyThrows
    void beforeEachTest() {
        // reset cache settings singleton
        Field instanceField = CacheApiSettings.getInstance().getClass().getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        instanceField.set(CacheApiSettings.getInstance(), new CacheApiSettings());
    }

    @Test
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
    void registerAndDefaultCacheProviderTest() {
        // register simple map provider
        CacheApiSettings.getInstance().registerCacheProvider(SimpleMapProvider.class, new SimpleMapProvider());

        // check default
        CacheProvider defaultCacheProvider = CacheApiSettings.getInstance().getDefaultCacheProvider();
        Assertions.assertEquals(SimpleMapProvider.class.getCanonicalName(), defaultCacheProvider.getClass().getCanonicalName());
    }

    @Test
    void setDefaultCacheProviderTest() {
        // set default cache provider
        CacheApiSettings.getInstance().setDefaultCacheProvider(new SimpleMapProvider());

        // check default
        CacheProvider defaultCacheProvider = CacheApiSettings.getInstance().getDefaultCacheProvider();
        Assertions.assertEquals(SimpleMapProvider.class.getCanonicalName(), defaultCacheProvider.getClass().getCanonicalName());
    }

    @Test
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
