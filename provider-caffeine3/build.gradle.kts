dependencies {
    api(project(":cache-core"))

    implementation("com.github.ben-manes.caffeine:caffeine:3.1.7")

    testImplementation(testFixtures(project(":cache-core")))
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

publishing.publications.withType<MavenPublication> {
    pom {
        name.set("Xanthic - Caffeine3 Provider Module")
        description.set("Xanthic Provider dependency for Caffeine3")
    }
}
