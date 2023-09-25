package io.github.xanthic.jackson.util;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * A serializable {@link Consumer} since {@link com.fasterxml.jackson.databind.cfg.CacheProvider}
 * must be {@link Serializable}, as it is stored in {@link com.fasterxml.jackson.databind.ObjectMapper}.
 *
 * @param <T> the type of the input for the consumer
 */
@FunctionalInterface
public interface SerializableConsumer<T> extends Consumer<T>, Serializable {}
