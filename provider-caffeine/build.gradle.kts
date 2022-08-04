dependencies {
    api(project(":cache-core"))

    implementation("com.github.ben-manes.caffeine:caffeine:2.9.3")

    testImplementation(testFixtures(project(":cache-core")))
}

publishing.publications.withType<MavenPublication> {
    pom {
        name.set("Xanthic - Caffeine Provider Module")
        description.set("Xanthic Provider dependency for Caffeine")
    }
}
