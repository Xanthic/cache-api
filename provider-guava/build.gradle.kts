dependencies {
    api(project(":cache-core"))

    implementation("com.google.guava:guava:33.2.0-jre")

    testImplementation(testFixtures(project(":cache-core")))
}

publishing.publications.withType<MavenPublication> {
    pom {
        name.set("Xanthic - Guava Provider Module")
        description.set("Xanthic Provider dependency for Guava")
    }
}
