dependencies {
    api(project(":cache-core"))

    implementation("androidx.collection:collection:1.4.3")

    testImplementation(testFixtures(project(":cache-core")))
}

publishing.publications.withType<MavenPublication> {
    pom {
        name.set("Xanthic - AndroidX Provider Module")
        description.set("Xanthic Provider dependency for AndroidX")
    }
}
