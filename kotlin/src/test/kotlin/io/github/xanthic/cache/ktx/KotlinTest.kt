package io.github.xanthic.cache.ktx

import io.github.xanthic.cache.api.domain.ExpiryType
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class KotlinTest {

    @Test
    fun buildTest() {
        val cache = createCache<String, Int> {
            maxSize = 69
            expiryType = ExpiryType.POST_ACCESS
            expiryTime = Duration.ofMillis(420)
            removalListener { key, value, cause ->
                if (cause.isEviction) {
                    println("evicted: $key:$value")
                }
            }
            executor = null // unnecessary, but demonstrates nullability
        }

        assertNotNull(cache)

        cache["420"] = 420
        assertEquals(420, cache["420"])

        cache -= "420"
        assertTrue("420" !in cache)

        cache += mapOf("1" to 1, "2" to 2)
        assertTrue("1" in cache)
        assertTrue("2" in cache)
    }

}
