package io.github.xanthic.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class XanthicJacksonCacheProviderTest {

	@Test
	void deserialize() throws JsonProcessingException {
		ObjectMapper mapper = JsonMapper.builder()
			.cacheProvider(XanthicJacksonCacheProvider.defaults())
			.build();
		Foo foo = mapper.readValue("{\"bar\":\"baz\"}", Foo.class);
		assertNotNull(foo);
		assertEquals("baz", foo.getBar());
	}

	@Test
	void serialize() throws JsonProcessingException {
		ObjectMapper mapper = JsonMapper.builder()
			.cacheProvider(XanthicJacksonCacheProvider.defaults())
			.build();
		String json = mapper.writeValueAsString(new Foo("baz"));
		assertEquals("{\"bar\":\"baz\"}", json);
	}

	@Test
	void constructType() {
		ObjectMapper mapper = JsonMapper.builder()
			.cacheProvider(XanthicJacksonCacheProvider.defaults())
			.build();
		JavaType type = mapper.getTypeFactory().constructParametricType(List.class, Integer.class);
		assertNotNull(type);
	}

	@Data
	@Setter(AccessLevel.PRIVATE)
	@NoArgsConstructor
	@AllArgsConstructor
	static class Foo {
		private String bar;
	}

}
