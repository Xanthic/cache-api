dependencies {
    api(project(":cache-core"))

    implementation("org.springframework.boot:spring-boot-starter-cache:2.7.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.7.3")
    testImplementation(testFixtures(project(":cache-core")))
    testImplementation(project(":cache-provider-caffeine"))
}

publishing.publications.withType<MavenPublication> {
    pom {
        name.set("Xanthic - Spring Cache Bridge")
        description.set("Xanthic Cache Spring Bridge")
    }
}
