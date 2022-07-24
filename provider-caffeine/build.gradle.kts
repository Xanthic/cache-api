dependencies {
    api(project(":cache-core"))

    implementation("com.github.ben-manes.caffeine:caffeine:3.1.1")

    testImplementation(testFixtures(project(":cache-core")))
}
