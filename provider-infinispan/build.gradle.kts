dependencies {
    api(project(":cache-core"))

    compileOnly("org.infinispan:infinispan-component-annotations:13.0.10.Final")
    implementation("org.infinispan:infinispan-core:13.0.10.Final")

    testImplementation(testFixtures(project(":cache-core")))
}

publishing.publications.withType<MavenPublication> {
    pom {
        name.set("Xanthic - Infinispan Provider Module")
        description.set("Xanthic Provider dependency for Infinispan")
    }
}
