dependencies {
    api(project(":cache-core"))

    implementation(platform("org.infinispan:infinispan-bom:13.0.15.Final"))

    compileOnly("org.infinispan:infinispan-component-annotations")
    implementation("org.infinispan:infinispan-core")

    testImplementation(testFixtures(project(":cache-core")))
}

publishing.publications.withType<MavenPublication> {
    pom {
        name.set("Xanthic - Infinispan Provider Module")
        description.set("Xanthic Provider dependency for Infinispan")
    }
}
