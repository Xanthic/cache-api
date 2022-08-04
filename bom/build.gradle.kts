plugins {
    `java-platform`
}

dependencies {
    constraints {
        api(project(":cache-api"))
        api(project(":cache-core"))
        api(project(":cache-kotlin"))
        api(project(":cache-provider-androidx"))
        api(project(":cache-provider-cache2k"))
        api(project(":cache-provider-caffeine"))
        api(project(":cache-provider-caffeine3"))
        api(project(":cache-provider-ehcache"))
        api(project(":cache-provider-expiringmap"))
        api(project(":cache-provider-guava"))
        api(project(":cache-provider-infinispan"))
    }
}

publishing.publications.withType<MavenPublication> {
    pom {
        name.set("Xanthic - BOM Platform")
        description.set("Xanthic Build of Materials dependency constraints")
    }
}
