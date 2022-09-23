package io.github.xanthic.cache.ktx

import io.github.xanthic.cache.api.Cache
import io.github.xanthic.cache.core.CacheApi
import io.github.xanthic.cache.core.CacheApiSpec

/**
 * @see CacheApi.create
 */
fun <K, V> createCache(init: CacheApiSpec<K, V>.() -> Unit): Cache<K, V> = CacheApi.create(init)

/**
 * @see Cache.get
 */
operator fun <K : Any, V : Any> Cache<K, V>.contains(key: K): Boolean = this.get(key) != null

/**
 * @see Cache.remove
 */
operator fun <K : Any, V : Any> Cache<K, V>.minusAssign(key: K) {
    this.remove(key)
}

/**
 * @see Cache.putAll
 */
operator fun <K : Any, V : Any> Cache<K, V>.plusAssign(map: Map<K, V>) {
    this.putAll(map)
}

/**
 * @see Cache.put
 */
operator fun <K : Any, V : Any> Cache<K, V>.set(key: K, value: V): V? = this.put(key, value)
