package io.github.xanthic.cache.ktx

import io.github.xanthic.cache.api.Cache
import io.github.xanthic.cache.core.CacheApi
import io.github.xanthic.cache.core.CacheApiSpec

/**
 * @see CacheApi.create
 */
fun <K, V> createCache(init: CacheApiSpec<K, V>.() -> Unit): Cache<K, V> = CacheApi.create(init)
