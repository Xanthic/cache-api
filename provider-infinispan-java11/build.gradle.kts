dependencies {
    api(project(":cache-core"))

    implementation(platform("org.infinispan:infinispan-bom:14.0.29.Final"))

    compileOnly("org.infinispan:infinispan-component-annotations")
    implementation("org.infinispan:infinispan-core")

    testImplementation(testFixtures(project(":cache-core")))
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

publishing.publications.withType<MavenPublication> {
    pom {
        name.set("Xanthic - Infinispan Provider Module for JDK 11")
        description.set("Xanthic Provider dependency for Infinispan on JDK 11+")
    }
}
