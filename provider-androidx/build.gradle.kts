dependencies {
    api(project(":cache-core"))

    implementation("androidx.collection:collection:1.2.0")

    testImplementation(project(":cache-core").dependencyProject.sourceSets.test.get().runtimeClasspath)
}
