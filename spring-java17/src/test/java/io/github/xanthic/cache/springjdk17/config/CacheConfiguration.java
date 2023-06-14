package io.github.xanthic.cache.springjdk17.config;

import io.github.xanthic.cache.api.domain.ExpiryType;
import io.github.xanthic.cache.springjdk17.XanthicSpringCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfiguration {

	@Bean
	public CacheManager cacheManager() {
		XanthicSpringCacheManager cacheManager = new XanthicSpringCacheManager(spec -> {
			spec.expiryType(ExpiryType.POST_ACCESS);
		});
		cacheManager.registerCache("my-custom-cache", spec -> {
			spec.maxSize(10L);
		});
		return cacheManager;
	}

}
