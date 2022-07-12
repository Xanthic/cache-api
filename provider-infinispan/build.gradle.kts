dependencies {
    api(project(":cache-core"))

    compileOnly("org.infinispan:infinispan-component-annotations:13.0.10.Final")
    implementation("org.infinispan:infinispan-core:13.0.10.Final")

    testImplementation(testFixtures(project(":cache-core")))
}
