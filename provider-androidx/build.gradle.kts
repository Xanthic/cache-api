dependencies {
    api(project(":cache-core"))

    implementation("androidx.collection:collection:1.2.0")

    testImplementation(testFixtures(project(":cache-core")))
}
