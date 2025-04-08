dependencies {
    api(project(":cache-core"))

    implementation("com.google.guava:guava:33.4.7-android")

    testImplementation(testFixtures(project(":cache-core")))
}

publishing.publications.withType<MavenPublication> {
    pom {
        name.set("Xanthic - Guava Provider Module")
        description.set("Xanthic Provider dependency for Guava")
    }
}
