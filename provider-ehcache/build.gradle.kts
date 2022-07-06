dependencies {
    api(project(":cache-core"))

    implementation("org.ehcache:ehcache:3.10.0")

    testImplementation(project(":cache-core").dependencyProject.sourceSets.test.get().runtimeClasspath)
}
