dependencies {
    api(project(":cache-core"))

    implementation("androidx.collection:collection:1.3.0-dev01")

    testImplementation(testFixtures(project(":cache-core")))
}

publishing.publications.withType<MavenPublication> {
    pom {
        name.set("Xanthic - AndroidX Provider Module")
        description.set("Xanthic Provider dependency for AndroidX")
    }
}
