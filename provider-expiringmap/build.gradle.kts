dependencies {
    api(project(":cache-core"))

    implementation("net.jodah:expiringmap:0.5.10")

    testImplementation(testFixtures(project(":cache-core")))
}
