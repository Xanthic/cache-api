rootProject.name = "cache"

include(
    ":bom",
    ":api",
    ":core",
    ":kotlin",
    ":spring",
    ":spring-java17",
    ":provider-androidx",
    ":provider-cache2k",
    ":provider-caffeine",
    ":provider-caffeine3",
    ":provider-ehcache",
    ":provider-expiringmap",
    ":provider-guava",
    ":provider-infinispan",
    ":provider-infinispan-java11",
)

project(":bom").name = "cache-bom"
project(":api").name = "cache-api"
project(":core").name = "cache-core"
project(":kotlin").name = "cache-kotlin"
project(":spring").name = "cache-spring"
project(":spring-java17").name = "cache-spring-java17"
project(":provider-androidx").name = "cache-provider-androidx"
project(":provider-cache2k").name = "cache-provider-cache2k"
project(":provider-caffeine").name = "cache-provider-caffeine"
project(":provider-caffeine3").name = "cache-provider-caffeine3"
project(":provider-ehcache").name = "cache-provider-ehcache"
project(":provider-expiringmap").name = "cache-provider-expiringmap"
project(":provider-guava").name = "cache-provider-guava"
project(":provider-infinispan").name = "cache-provider-infinispan"
project(":provider-infinispan-java11").name = "cache-provider-infinispan-java11"
