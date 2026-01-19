plugins {
    `java-test-fixtures`
}

dependencies {
    api(project(":cache-api"))

    testFixturesImplementation(platform("org.junit:junit-bom:6.0.2"))
    testFixturesImplementation("org.junit.jupiter:junit-jupiter")
    testFixturesRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testFixturesImplementation("org.awaitility:awaitility:4.3.0")
}

publishing.publications.withType<MavenPublication> {
    pom {
        name.set("Xanthic - Core Module")
        description.set("Xanthic Cache Core dependency")
    }
}
