<img src=".github/logo.png?raw=true" alt="Xanthic logo" width="500" />

[![Latest](https://img.shields.io/github/release/Xanthic/cache-api/all.svg?style=flate&label=latest)](https://search.maven.org/search?q=g:io.github.xanthic.cache)
[![Build](https://github.com/Xanthic/cache-api/actions/workflows/gradle.yml/badge.svg)](https://github.com/Xanthic/cache-api/actions/workflows/gradle.yml)
[![Documentation](https://img.shields.io/badge/documentation-grey.svg?style=flat)](https://Xanthic.github.io/)
[![Javadoc](https://javadoc.io/badge2/io.github.xanthic.cache/cache-api/javadoc.svg)](https://javadoc.io/doc/io.github.xanthic.cache)

[![Code Quality](https://www.codefactor.io/repository/github/xanthic/cache-api/badge)](https://www.codefactor.io/repository/github/xanthic/cache-api)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Xanthic_cache-api&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=Xanthic_cache-api)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Xanthic_cache-api&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=Xanthic_cache-api)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Xanthic_cache-api&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=Xanthic_cache-api)

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

|                                     Backend                                      |       Provider        |              Artifact              |
|:--------------------------------------------------------------------------------:|:---------------------:|:----------------------------------:|
|              [Caffeine](https://github.com/ben-manes/caffeine/wiki)              |  `CaffeineProvider`   |     `cache-provider-caffeine`      |
|             [Caffeine3](https://github.com/ben-manes/caffeine/wiki)              |  `Caffeine3Provider`  |     `cache-provider-caffeine3`     |
|          [Guava](https://github.com/google/guava/wiki/CachesExplained)           |    `GuavaProvider`    |       `cache-provider-guava`       |
|                          [Cache2k](https://cache2k.org)                          |   `Cache2kProvider`   |      `cache-provider-cache2k`      |
| [AndroidX](https://developer.android.com/reference/androidx/collection/LruCache) | `AndroidLruProvider`  |     `cache-provider-androidx`      |
|       [ExpiringMap](https://github.com/jhalterman/expiringmap#expiringmap)       | `ExpiringMapProvider` |    `cache-provider-expiringmap`    |
|    [Ehcache v3 (heap)](https://www.ehcache.org/documentation/3.0/index.html)     |   `EhcacheProvider`   |      `cache-provider-ehcache`      |
|            [Infinispan (heap)](https://infinispan.org/documentation/)            | `InfinispanProvider`  |    `cache-provider-infinispan`     |
|          [Infinispan v14 (heap)](https://infinispan.org/documentation/)          | `InfinispanProvider`  | `cache-provider-infinispan-java11` |
|          [Infinispan v15 (heap)](https://infinispan.org/documentation/)          | `InfinispanProvider`  | `cache-provider-infinispan-java17` |

Don't see your preferred implementation listed above?
Fear not, it is not difficult to create your own binding, and we'd be happy to accept it in a PR!

## Installation

We publish to [Maven Central](https://search.maven.org/search?q=g:io.github.xanthic.cache) and provide a convenient BOM (Build of Materials) to keep dependency versions in sync from the api to the provider.

Library developers only need to depend on the `cache-core` artifact, allowing application developers to specify which [provider](#supported-implementations) to use at runtime.

### Gradle (Kotlin)

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    api(platform("io.github.xanthic.cache:cache-bom:0.6.2")) // Specify the latest version here
    api(group = "io.github.xanthic.cache", name = "cache-core") // For library devs
    implementation(group = "io.github.xanthic.cache", name = "cache-provider-caffeine") // For application devs; can select any provider
}
```

### Maven

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.xanthic.cache</groupId>
            <artifactId>cache-bom</artifactId>
            <!-- Specify the latest version here -->
            <version>0.6.2</version>
            <scope>import</scope>
            <type>pom</type>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- For library devs -->
    <dependency>
      <groupId>io.github.xanthic.cache</groupId>
      <artifactId>cache-core</artifactId>
    </dependency>
    
    <!-- For application devs (can select any provider) -->
    <dependency>
      <groupId>io.github.xanthic.cache</groupId>
      <artifactId>cache-provider-caffeine</artifactId>
    </dependency>
</dependencies>
```

## Example Usage

Users should include at least one [provider](#supported-implementations) module in the runtime class-path.
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

Note: Kotlin users can enjoy an [even cleaner](kotlin/src/test/kotlin/io/github/xanthic/cache/ktx/KotlinTest.kt) syntax via the [extensions module](https://search.maven.org/search?q=g:io.github.xanthic.cache%20a:cache-kotlin)!

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

### What's in a name?

The library name, Xanthic, is a reference to [Xanthine](https://en.wikipedia.org/wiki/Xanthine).
Taking inspiration from the [Caffeine](https://github.com/ben-manes/caffeine/wiki) cache library, Xanthine broadly includes the class of methylated xanthines that numerous cognitive stimulants (such as caffeine) belong to.
Further, these compounds have been shown to [improve](https://doi.org/10.1002/hup.218) memory recall, creating an apt parallel for this library.
