package io.github.xanthic.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CacheProvider;
import com.fasterxml.jackson.databind.deser.DeserializerCache;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.ser.SerializerCache;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.github.xanthic.jackson.util.TrackedCacheProvider;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XanthicJacksonCacheProviderTest {

	@Test
	void defaults() throws JsonProcessingException {
		ObjectMapper mapper = JsonMapper.builder()
			.cacheProvider(XanthicJacksonCacheProvider.defaults())
			.build();
		assertNotNull(mapper.readValue("{\"bar\":\"baz\"}", Foo.class));
		assertNotNull(mapper.writeValueAsString(new Foo("baz")));
		assertNotNull(mapper.getTypeFactory().constructParametricType(List.class, Integer.class));
	}

	@Test
	void deserialize() throws JsonProcessingException {
		TrackedCacheProvider provider = new TrackedCacheProvider();
		ObjectMapper mapper = JsonMapper.builder()
			.cacheProvider(createCacheProvider(provider))
			.build();
		assertFalse(provider.getConstructedCaches().stream().anyMatch(c -> c.size() > 0));
		Foo foo = mapper.readValue("{\"bar\":\"baz\"}", Foo.class);
		assertNotNull(foo);
		assertEquals("baz", foo.getBar());
		assertTrue(provider.getConstructedCaches().stream().anyMatch(c -> c.size() > 0));
	}

	@Test
	void serialize() throws JsonProcessingException {
		TrackedCacheProvider provider = new TrackedCacheProvider();
		ObjectMapper mapper = JsonMapper.builder()
			.cacheProvider(createCacheProvider(provider))
			.build();
		assertFalse(provider.getConstructedCaches().stream().anyMatch(c -> c.size() > 0));
		String json = mapper.writeValueAsString(new Foo("baz"));
		assertEquals("{\"bar\":\"baz\"}", json);
		assertTrue(provider.getConstructedCaches().stream().anyMatch(c -> c.size() > 0));
	}

	@Test
	void constructType() {
		TrackedCacheProvider provider = new TrackedCacheProvider();
		ObjectMapper mapper = JsonMapper.builder()
			.cacheProvider(createCacheProvider(provider))
			.build();
		assertFalse(provider.getConstructedCaches().stream().anyMatch(c -> c.size() > 0));
		JavaType type = mapper.getTypeFactory().constructParametricType(List.class, Integer.class);
		assertNotNull(type);
		assertTrue(provider.getConstructedCaches().stream().anyMatch(c -> c.size() > 0));
	}

	@Test
	void constructMultiple() throws JsonProcessingException {
		TrackedCacheProvider provider = new TrackedCacheProvider();
		assertEquals(0, provider.getConstructedCaches().size());

		ObjectMapper m1 = JsonMapper.builder().cacheProvider(createCacheProvider(provider)).build();
		m1.readValue("{\"bar\":\"baz\"}", Foo.class);
		m1.writeValueAsString(new Foo("baz"));
		m1.getTypeFactory().constructParametricType(List.class, Integer.class);
		assertEquals(3, provider.getConstructedCaches().size());
		assertTrue(provider.getConstructedCaches().stream().allMatch(c -> c.size() > 0));

		ObjectMapper m2 = JsonMapper.builder().cacheProvider(createCacheProvider(provider)).build();
		m2.readValue("{\"bar\":\"baz\"}", Foo.class);
		m2.writeValueAsString(new Foo("baz"));
		m2.getTypeFactory().constructParametricType(List.class, Integer.class);
		assertEquals(6, provider.getConstructedCaches().size());
		assertTrue(provider.getConstructedCaches().stream().allMatch(c -> c.size() > 0));
	}

	private static CacheProvider createCacheProvider(TrackedCacheProvider trackedProvider) {
		return new XanthicJacksonCacheProvider(
			spec -> spec.provider(trackedProvider).maxSize((long) DeserializerCache.DEFAULT_MAX_CACHE_SIZE),
			spec -> spec.provider(trackedProvider).maxSize((long) SerializerCache.DEFAULT_MAX_CACHE_SIZE),
			spec -> spec.provider(trackedProvider).maxSize((long) TypeFactory.DEFAULT_MAX_CACHE_SIZE)
		);
	}

	@Data
	@Setter(AccessLevel.PRIVATE)
	@NoArgsConstructor
	@AllArgsConstructor
	static class Foo {
		private String bar;
	}

}
