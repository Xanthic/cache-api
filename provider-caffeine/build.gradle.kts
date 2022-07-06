dependencies {
    api(project(":cache-core"))

    implementation("com.github.ben-manes.caffeine:caffeine:2.9.3")

    testImplementation(project(":cache-core").dependencyProject.sourceSets.test.get().runtimeClasspath)
}
