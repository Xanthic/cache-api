dependencies {
    api(project(":cache-core"))

    implementation("org.ehcache:ehcache:3.10.0")

    testImplementation(testFixtures(project(":cache-core")))
}
