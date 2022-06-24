package io.github.iprodigy.cache.provider.infinispan;

import io.github.iprodigy.cache.api.Cache;
import io.github.iprodigy.cache.core.CacheApi;
import io.github.iprodigy.cache.core.CacheApiSettings;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

@Slf4j
public class InfinispanProviderTest {

    @Test
    void putGetClearTest() {
        Cache<String, Integer> cache = CacheApi.create(spec -> {
            spec.provider(new InfinispanProvider());
            spec.maxSize(32L);
            spec.expiryTime(Duration.ofMinutes(1));
            // spec.removalListener((key, value, cause) -> log.info(key + ":" + value + "=" + cause));
            // TODO: fix org.infinispan.notifications.IncorrectListenerException: Cache listener class io.github.iprodigy.cache.provider.infinispan.InfinispanProvider$InfinispanListener must be annotated with org.infinispan.notifications.Listener
        });

        cache.put("4/20", 420);
        Assertions.assertEquals(420, cache.get("4/20"));
        cache.clear();
        Assertions.assertNull(cache.get("4/20"));
    }

    @Test
    void testRegisteredAsDefault() {
        Assertions.assertEquals(InfinispanProvider.class.getCanonicalName(), CacheApiSettings.getInstance().getDefaultCacheProvider().getClass().getCanonicalName());
    }

}
