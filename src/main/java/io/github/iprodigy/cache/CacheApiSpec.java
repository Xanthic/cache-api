package io.github.iprodigy.cache;

import io.github.iprodigy.cache.providers.CacheProvider;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

@Accessors(chain = true, fluent = true, prefix = "")
@Setter
@Getter
public class CacheApiSpec<K, V> {

    public static <K, V> @NotNull CacheApiSpec<K, V> process(@NotNull Consumer<CacheApiSpec<K, V>> spec) {
        CacheApiSpec<K, V> data = new CacheApiSpec<>();
        spec.accept(data);
        data.validate();
        return data;
    }

    /**
     * validate the config
     *
     * TODO: proper validation
     */
    public void validate() {
        Objects.requireNonNull(provider, "provider may not be null!");
        Objects.requireNonNull(maxSize, "maxSize may not be null!");
        // Objects.requireNonNull(expiryTime, "expiryTime may not be null!");
        // Objects.requireNonNull(expiryType, "expiryType may not be null!");
        // Objects.requireNonNull(removalListener, "removalListener may not be null!");
        // Objects.requireNonNull(executor, "executor may not be null!");
    }

    private CacheProvider provider = CacheApiSettings.getInstance().getDefaultCacheProvider();

    private Long maxSize;

    private Duration expiryTime;

    private ExpiryType expiryType;

    private RemovalListener<K, V> removalListener;

    private ScheduledExecutorService executor;

}