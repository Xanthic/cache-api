dependencies {
    api(project(":cache-core"))

    implementation("org.ehcache:ehcache:3.11.1")

    testImplementation(testFixtures(project(":cache-core")))
}

publishing.publications.withType<MavenPublication> {
    pom {
        name.set("Xanthic - Ehcache Provider Module")
        description.set("Xanthic Provider dependency for Ehcache")
    }
}
