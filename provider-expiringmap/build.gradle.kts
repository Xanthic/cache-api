dependencies {
    api(project(":cache-core"))

    implementation("net.jodah:expiringmap:0.5.10")

    testImplementation(testFixtures(project(":cache-core")))
}

publishing.publications.withType<MavenPublication> {
    pom {
        name.set("Xanthic - ExpiringMap Provider Module")
        description.set("Xanthic Provider dependency for ExpiringMap")
    }
}
