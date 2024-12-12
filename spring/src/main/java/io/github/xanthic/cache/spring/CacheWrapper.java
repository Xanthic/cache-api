package io.github.xanthic.cache.spring;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import org.springframework.cache.Cache;

@Value
@Accessors(fluent = true)
@RequiredArgsConstructor
class CacheWrapper {
	Cache cache;
	boolean custom;

	CacheWrapper(Cache cache) {
		this(cache, false);
	}
}
