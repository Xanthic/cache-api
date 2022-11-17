dependencies {
    api(project(":cache-core"))

    implementation("org.ehcache:ehcache:3.10.5")

    testImplementation(testFixtures(project(":cache-core")))
}

publishing.publications.withType<MavenPublication> {
    pom {
        name.set("Xanthic - Ehcache Provider Module")
        description.set("Xanthic Provider dependency for Ehcache")
    }
}
