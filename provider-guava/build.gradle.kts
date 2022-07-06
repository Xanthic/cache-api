dependencies {
    api(project(":cache-core"))

    implementation("com.google.guava:guava:31.1-jre")

    testImplementation(project(":cache-core").dependencyProject.sourceSets.test.get().output)
}
