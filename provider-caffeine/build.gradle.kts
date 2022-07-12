dependencies {
    api(project(":cache-core"))

    implementation("com.github.ben-manes.caffeine:caffeine:2.9.3")

    testImplementation(testFixtures(project(":cache-core")))
}
