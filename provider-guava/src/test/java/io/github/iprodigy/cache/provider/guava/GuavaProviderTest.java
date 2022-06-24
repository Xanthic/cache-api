package io.github.iprodigy.cache.provider.guava;

import io.github.iprodigy.cache.api.Cache;
import io.github.iprodigy.cache.core.CacheApi;
import io.github.iprodigy.cache.core.CacheApiSettings;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

@Slf4j
public class GuavaProviderTest {

    @Test
    void putGetClearTest() {
        Cache<String, Integer> cache = CacheApi.create(spec -> {
            spec.provider(new GuavaProvider());
            spec.maxSize(32L);
            spec.expiryTime(Duration.ofMinutes(1));
            spec.removalListener((key, value, cause) -> log.info(key + ":" + value + "=" + cause));
        });

        cache.put("4/20", 420);
        Assertions.assertEquals(420, cache.get("4/20"));
        cache.clear();
        Assertions.assertNull(cache.get("4/20"));
    }

    @Test
    void testRegisteredAsDefault() {
        Assertions.assertEquals(GuavaProvider.class.getCanonicalName(), CacheApiSettings.getInstance().getDefaultCacheProvider().getClass().getCanonicalName());
    }

}
