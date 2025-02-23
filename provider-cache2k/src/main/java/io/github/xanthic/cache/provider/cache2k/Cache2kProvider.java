package io.github.xanthic.cache.provider.cache2k;

import io.github.xanthic.cache.api.Cache;
import io.github.xanthic.cache.api.ICacheSpec;
import io.github.xanthic.cache.api.RemovalListener;
import io.github.xanthic.cache.api.domain.ExpiryType;
import io.github.xanthic.cache.api.domain.RemovalCause;
import io.github.xanthic.cache.core.AbstractCacheProvider;
import org.cache2k.Cache2kBuilder;
import org.cache2k.event.CacheEntryEvictedListener;
import org.cache2k.event.CacheEntryExpiredListener;
import org.cache2k.event.CacheEntryOperationListener;
import org.cache2k.event.CacheEntryRemovedListener;
import org.cache2k.event.CacheEntryUpdatedListener;
import org.cache2k.operation.Scheduler;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Provides {@link Cache} instances using {@link Cache2kBuilder}.
 * <p>
 * Implements size and time-based eviction, but {@link ExpiryType#POST_ACCESS} is
 * <a href="https://cache2k.org/docs/latest/apidocs/cache2k-api/org/cache2k/Cache2kBuilder.html#idleScanTime(java.time.Duration)">imprecisely</a> handled.
 */
public final class Cache2kProvider extends AbstractCacheProvider {

	@Override
	public <K, V> Cache<K, V> build(ICacheSpec<K, V> spec) {
		//noinspection unchecked
		Cache2kBuilder<K, V> builder = (Cache2kBuilder<K, V>) Cache2kBuilder.forUnknownTypes()
			.disableStatistics(true) // avoid performance penalty since we don't offer an interface for these statistics
			.boostConcurrency(Boolean.TRUE.equals(spec.highContention())); // utilize more memory to optimize for many threads performing mutations

		if (spec.maxSize() != null) {
			builder.entryCapacity(spec.maxSize());
		} else {
			// We must specify MAX_VALUE to create an unbounded cache to comply with the Xanthic maxSize spec
			// since Cache2k, by default, imposes a capacity bound of 1802 (Cache2kConfig#DEFAULT_ENTRY_CAPACITY)
			builder.entryCapacity(Long.MAX_VALUE);
		}

		ScheduledExecutorService exec = populateExecutor(builder, spec.executor());

		buildListeners(spec.removalListener()).forEach(li -> {
			if (exec != null) {
				builder.addAsyncListener(li);
			} else {
				builder.addListener(li);
			}
		});

		if (spec.expiryTime() == null) {
			builder.eternal(true);
		} else {
			handleExpiration(spec.expiryTime(), spec.expiryType(), (time, type) -> {
				if (type == ExpiryType.POST_WRITE) {
					builder.expireAfterWrite(time);
				} else {
					long t = time.toNanos();
					builder.idleScanTime(t / 3 * 2 + (t % 3 == 0 ? 0 : 1), TimeUnit.NANOSECONDS); // https://github.com/cache2k/cache2k/issues/39
				}

				if (exec != null)
					builder.sharpExpiry(true);
			});
		}

		return new Cache2kDelegate<>(builder.build());
	}

	@Override
	protected ExpiryType preferredType() {
		return ExpiryType.POST_WRITE; // POST_ACCESS isn't precisely offered out-of-the-box by cache2k
	}

	private static <K, V> ScheduledExecutorService populateExecutor(Cache2kBuilder<K, V> builder, ScheduledExecutorService exec) {
		if (exec == null) return null;
		builder.executor(exec);
		builder.loaderExecutor(exec);
		builder.refreshExecutor(exec);
		builder.asyncListenerExecutor(exec);
		builder.scheduler(new Scheduler() {
			@Override
			public void schedule(@NotNull Runnable runnable, long delayMillis) {
				exec.schedule(runnable, delayMillis, TimeUnit.MILLISECONDS);
			}

			@Override
			public void execute(@NotNull Runnable command) {
				exec.execute(command);
			}
		});
		return exec;
	}

	private static <K, V> Collection<CacheEntryOperationListener<K, V>> buildListeners(RemovalListener<K, V> listener) {
		if (listener == null) return Collections.emptyList();
		return Arrays.asList(
			(CacheEntryEvictedListener<K, V>) (cache, entry) -> listener.onRemoval(entry.getKey(), entry.getValue(), RemovalCause.SIZE),
			(CacheEntryExpiredListener<K, V>) (cache, entry) -> listener.onRemoval(entry.getKey(), entry.getValue(), RemovalCause.TIME),
			(CacheEntryRemovedListener<K, V>) (cache, entry) -> listener.onRemoval(entry.getKey(), entry.getValue(), RemovalCause.MANUAL),
			(CacheEntryUpdatedListener<K, V>) (cache, entry, newEntry) -> listener.onRemoval(entry.getKey(), entry.getValue(), RemovalCause.REPLACED)
		);
	}

	@Override
	public int getDiscoveryOrder() {
		return 4;
	}
}
