dependencies {
    api(project(":cache-core"))

    implementation("org.cache2k:cache2k-core:2.6.1.Final")

    testImplementation(testFixtures(project(":cache-core")))
}

publishing.publications.withType<MavenPublication> {
    pom {
        name.set("Xanthic - Cache2k Provider Module")
        description.set("Xanthic Provider dependency for Cache2k")
    }
}
