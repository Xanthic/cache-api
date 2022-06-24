package io.github.iprodigy.cache.core;

import io.github.iprodigy.cache.api.CacheProvider;
import io.github.iprodigy.cache.core.provider.SimpleMapProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class CacheRegistrationTest {

    @Test
    void registerAndDefaultCacheProviderTest() {
        // register simple map provider
        CacheApiSettings.getInstance().registerCacheProvider(SimpleMapProvider.class, new SimpleMapProvider());

        // check default
        CacheProvider defaultCacheProvider = CacheApiSettings.getInstance().getDefaultCacheProvider();
        Assertions.assertEquals("io.github.iprodigy.cache.core.provider.SimpleMapProvider", defaultCacheProvider.getClass().getCanonicalName());
    }

}
