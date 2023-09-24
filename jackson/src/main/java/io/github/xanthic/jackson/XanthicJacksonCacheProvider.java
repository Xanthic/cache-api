package io.github.xanthic.jackson;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.cfg.CacheProvider;
import com.fasterxml.jackson.databind.ser.SerializerCache;
import com.fasterxml.jackson.databind.util.LookupCache;
import com.fasterxml.jackson.databind.util.TypeKey;
import io.github.xanthic.cache.core.CacheApiSpec;
import io.github.xanthic.jackson.util.SerializableConsumer;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
public class XanthicJacksonCacheProvider implements CacheProvider {
	private static final long serialVersionUID = 1L;

	/**
	 * Specification for the deserializer cache.
	 */
	SerializableConsumer<CacheApiSpec<JavaType, JsonDeserializer<Object>>> deserializationSpec;

	/**
	 * Specification for the serializer cache.
	 */
	SerializableConsumer<CacheApiSpec<TypeKey, JsonSerializer<Object>>> serializationSpec;

	/**
	 * Specification for the type factory cache.
	 */
	SerializableConsumer<CacheApiSpec<Object, JavaType>> typeFactorySpec;

	/**
	 * Creates a Jackson {@link CacheProvider} backed by Xanthic, using the specified max cache sizes.
	 *
	 * @param maxDeserializerCacheSize the maximum size of the deserializer cache
	 * @param maxSerializerCacheSize   the maximum size of the serializer cache
	 * @param maxTypeFactoryCacheSize  the maximum size of the type factory cache
	 */
	public XanthicJacksonCacheProvider(long maxDeserializerCacheSize, long maxSerializerCacheSize, long maxTypeFactoryCacheSize) {
		this.deserializationSpec = spec -> spec.maxSize(maxDeserializerCacheSize);
		this.serializationSpec = spec -> spec.maxSize(maxSerializerCacheSize);
		this.typeFactorySpec = spec -> spec.maxSize(maxTypeFactoryCacheSize);
	}

	/**
	 * Creates a Jackson {@link CacheProvider} backed by Xanthic, using Jackson's recommended default max cache sizes.
	 */
	public XanthicJacksonCacheProvider() {
		// TODO: this(DeserializerCache.DEFAULT_MAX_CACHE_SIZE, SerializerCache.DEFAULT_MAX_CACHE_SIZE, TypeFactory.DEFAULT_MAX_CACHE_SIZE);
		this(2000, SerializerCache.DEFAULT_MAX_CACHED, 200);
	}

	@Override
	public LookupCache<JavaType, JsonDeserializer<Object>> forDeserializerCache(DeserializationConfig config) {
		return new XanthicJacksonCacheAdapter<>(deserializationSpec);
	}

	@Override
	public LookupCache<TypeKey, JsonSerializer<Object>> forSerializerCache(SerializationConfig config) {
		return new XanthicJacksonCacheAdapter<>(serializationSpec);
	}

	@Override
	public LookupCache<Object, JavaType> forTypeFactory() {
		return new XanthicJacksonCacheAdapter<>(typeFactorySpec);
	}
}
