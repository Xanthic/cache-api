dependencies {
    api(project(":cache-core"))

    implementation("org.cache2k:cache2k-core:2.6.1.Final")

    testImplementation(testFixtures(project(":cache-core")))
}
