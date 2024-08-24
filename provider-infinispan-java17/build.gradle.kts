dependencies {
    api(project(":cache-core"))

    implementation(platform("org.infinispan:infinispan-bom:15.0.8.Final"))

    compileOnly("org.infinispan:infinispan-component-annotations")
    implementation("org.infinispan:infinispan-core")

    testImplementation(testFixtures(project(":cache-core")))
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

publishing.publications.withType<MavenPublication> {
    pom {
        name.set("Xanthic - Infinispan Provider Module for JDK 17")
        description.set("Xanthic Provider dependency for Infinispan on JDK 17+")
    }
}
