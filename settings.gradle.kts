rootProject.name = "cache"

include(
    ":api",
    ":core",
    ":provider-androidx",
    ":provider-cache2k",
    ":provider-caffeine",
    ":provider-caffeine3",
    ":provider-ehcache",
    ":provider-expiringmap",
    ":provider-guava",
    ":provider-infinispan",
)

project(":api").name = "cache-api"
project(":core").name = "cache-core"
project(":provider-androidx").name = "cache-provider-androidx"
project(":provider-cache2k").name = "cache-provider-cache2k"
project(":provider-caffeine").name = "cache-provider-caffeine"
project(":provider-caffeine3").name = "cache-provider-caffeine3"
project(":provider-ehcache").name = "cache-provider-ehcache"
project(":provider-expiringmap").name = "cache-provider-expiringmap"
project(":provider-guava").name = "cache-provider-guava"
project(":provider-infinispan").name = "cache-provider-infinispan"
