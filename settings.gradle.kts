rootProject.name = "cache"

include(
    ":api",
    ":core",
    ":provider-androidx",
    ":provider-caffeine2",
    ":provider-ehcache",
    ":provider-expiringmap",
    ":provider-guava",
    ":provider-infinispan",
)

project(":api").name = "cache-api"
project(":core").name = "cache-core"
project(":provider-androidx").name = "cache-provider-androidx"
project(":provider-caffeine2").name = "cache-provider-caffeine2"
project(":provider-ehcache").name = "cache-provider-ehcache"
project(":provider-expiringmap").name = "cache-provider-expiringmap"
project(":provider-guava").name = "cache-provider-guava"
project(":provider-infinispan").name = "cache-provider-infinispan"
