package io.github.xanthic.cache.ktx

import io.github.xanthic.cache.api.domain.ExpiryType
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertNotNull

internal class KotlinTest {

    @Test
    fun buildTest() {
        val cache = createCache<String, Int> {
            maxSize = 69L
            expiryType = ExpiryType.POST_ACCESS
            expiryTime = Duration.ofMillis(420L)
            removalListener { key, value, cause ->
                if (cause.isEviction) {
                    println("evicted: $key:$value")
                }
            }
            executor = null // unnecessary, but demonstrates nullability
        }

        assertNotNull(cache)
    }

}
