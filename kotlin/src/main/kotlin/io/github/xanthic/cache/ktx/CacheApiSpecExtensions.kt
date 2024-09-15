@file:Suppress("unused")

package io.github.xanthic.cache.ktx

import io.github.xanthic.cache.api.CacheProvider
import io.github.xanthic.cache.api.RemovalListener
import io.github.xanthic.cache.api.domain.ExpiryType
import io.github.xanthic.cache.core.CacheApiSpec
import java.time.Duration
import java.util.concurrent.ScheduledExecutorService

// Receiver-type builder

/**
 * @see CacheApiSpec.process
 */
fun <K, V> processSpec(init: CacheApiSpec<K, V>.() -> Unit): CacheApiSpec<K, V> = CacheApiSpec.process(init)

// Redefine properties in kotlin-friendly format

/**
 * @see io.github.xanthic.cache.api.ICacheSpec.provider
 */
var <K, V> CacheApiSpec<K, V>.provider: CacheProvider
    get() = this.provider()
    set(value) {
        this.provider(value)
    }

/**
 * @see io.github.xanthic.cache.api.ICacheSpec.maxSize
 */
var <K, V> CacheApiSpec<K, V>.maxSize: Long?
    get() = this.maxSize()
    set(value) {
        this.maxSize(value)
    }

/**
 * @see io.github.xanthic.cache.api.ICacheSpec.expiryType
 */
var <K, V> CacheApiSpec<K, V>.expiryType: ExpiryType?
    get() = this.expiryType()
    set(value) {
        this.expiryType(value)
    }

/**
 * @see io.github.xanthic.cache.api.ICacheSpec.expiryTime
 */
var <K, V> CacheApiSpec<K, V>.expiryTime: Duration?
    get() = this.expiryTime()
    set(value) {
        this.expiryTime(value)
    }

/**
 * @see io.github.xanthic.cache.api.ICacheSpec.removalListener
 */
var <K, V> CacheApiSpec<K, V>.removalListener: RemovalListener<K, V>?
    get() = this.removalListener()
    set(value) {
        this.removalListener(value)
    }

/**
 * @see io.github.xanthic.cache.api.ICacheSpec.executor
 */
var <K, V> CacheApiSpec<K, V>.executor: ScheduledExecutorService?
    get() = this.executor()
    set(value) {
        this.executor(value)
    }

/**
 * @see io.github.xanthic.cache.api.ICacheSpec.highContention
 */
var <K, V> CacheApiSpec<K, V>.highContention: Boolean?
    get() = this.highContention()
    set(value) {
        this.highContention(value)
    }
