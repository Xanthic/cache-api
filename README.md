<img src=".github/logo.png?raw=true" alt="Xanthic logo" width="652" />

# Cache API

This library provides a simplified interface for interacting with in-memory cache implementations on the JVM.
Think: "SLF4J but for caching"

## Motivation

### For Application Developers

Need a simple in-memory cache for your application?
This library allows you to easily switch between different backing implementations whenever you desire!

### For Library Developers

Need caching in your library? Don't want to marry a single<sup>1</sup> implementation?
Simply code against this straightforward API and users can choose whichever backing implementation they prefer!

*<sup>1</sup> Even if you prefer a specific implementation (we like Caffeine, for example), this choice may not be compatible for all environments (Caffeine doesn't play well with Android, for
example), so it is safer to code against this API for long-term flexibility*

## Supported Implementations

The following backing cache implementations have bindings already provided by this library:

* [Caffeine](https://github.com/ben-manes/caffeine/wiki) via `CaffeineProvider` or `Caffeine3Provider`
* [Guava](https://github.com/google/guava/wiki/CachesExplained) via `GuavaProvider`
* [Cache2k](https://cache2k.org) via `Cache2kProvider`
* [AndroidX](https://developer.android.com/reference/androidx/collection/LruCache) via `AndroidExpiringLruProvider` or `AndroidLruProvider`
* [ExpiringMap](https://github.com/jhalterman/expiringmap#expiringmap) via `ExpiringMapProvider`
* [Ehcache v3 (heap)](https://www.ehcache.org/documentation/3.0/index.html) via `EhcacheProvider`
* [Infinispan (heap)](https://infinispan.org/documentation/) via `InfinispanProvider`

Don't see your preferred implementation listed above?
Fear not, it is not difficult to create your own binding, and we'd be happy to accept it in a PR!

## Example Usage

Users should include at least one provider module in the runtime class-path.
Further, they can (optionally) do (but replace `CaffeineProvider` with the desired provider):

```java
CacheApiSettings.getInstance().setDefaultCacheProvider(new CaffeineProvider());
```

Define a generic cache:

```java
Cache<String, Integer> cache = CacheApi.create(spec -> {
	spec.maxSize(2048L); // setting a size constraint is highly recommended
	spec.expiryTime(Duration.ofMinutes(5L));
	spec.expiryType(ExpiryType.POST_ACCESS);
	spec.removalListener((key, value, cause) -> {
		if (cause.isEviction()) {
			// do something
		}
	});
});
```

Here, the default provider will be used as `CacheBuilder#provider(CacheProvider)` was not called (note: this builder option is meant for end users rather than library devs).

Aside: the `removalListener` in the example above technically has no effect, but is included for illustration.

## WIP

This API is still in alpha development stage. The current TODO list includes:

- [x] Add Javadocs
- [ ] Incorporate logging (via SLF4J)
- [x] Create test suite (via JUnit)
- [ ] Consider if any more bindings should be added for initial release
- [ ] Eventually: Publish to Maven

## FAQ

### Why not JCache?

As the JCache spec attempts to cover many other features from distributed caching to non-heap storage, it is less performant and arguably more bloated.
Here, we simply focus on the heap caching scenario (as Caffeine does), which allows for a much more straightforward interface to interact with.
As a result, our API can support many more implementations than are covered by JCache, including non-server-side use cases like Android.

### Where did the idea come from?

As a maintainer of the [twitch4j](https://github.com/twitch4j/twitch4j) library, we used Caffeine as our preferred high-performance, in-memory cache across our major modules.
While this worked well for the server-side use case, Android users later reported incompatibilities, rendering twitch4j unusable for this platform.
Thus spawned the desire for a simple cache api where specific implementations can be dynamically chosen at runtime by the end developer's choice
(allowing some to use Caffeine while others can use LruCache, for example).
