package io.github.iprodigy.cache.core;

import io.github.iprodigy.cache.api.Cache;
import io.github.iprodigy.cache.api.CacheProvider;
import io.github.iprodigy.cache.core.provider.SimpleMapProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

@Slf4j
public class CacheRegistrationTest {

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
