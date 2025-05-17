dependencies {
    api(project(":cache-core"))
    implementation("org.springframework:spring-context:6.2.7")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.4.5")
    testImplementation("org.awaitility:awaitility:4.3.0")
    testImplementation(testFixtures(project(":cache-core")))
    testImplementation(project(":cache-provider-caffeine"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

publishing.publications.withType<MavenPublication> {
    pom {
        name.set("Xanthic - Spring for JDK 17")
        description.set("Xanthic Cache Spring on JDK 17+")
    }
}
