dependencies {
    api(project(":cache-core"))

    implementation("org.cache2k:cache2k-core:2.6.1.Final")

    testImplementation(project(":cache-core").dependencyProject.sourceSets.test.get().output)
}
