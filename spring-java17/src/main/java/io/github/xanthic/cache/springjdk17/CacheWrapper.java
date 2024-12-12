package io.github.xanthic.cache.springjdk17;

import org.springframework.cache.Cache;

record CacheWrapper(Cache cache, boolean custom) {
	CacheWrapper(Cache cache) {
		this(cache, false);
	}
}
