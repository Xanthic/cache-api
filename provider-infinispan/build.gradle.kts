dependencies {
    api(project(":cache-core"))

    compileOnly("org.infinispan:infinispan-component-annotations:14.0.0.Dev04")
    implementation("org.infinispan:infinispan-core:13.0.10.Final")

    testImplementation(testFixtures(project(":cache-core")))
}
