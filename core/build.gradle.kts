plugins {
    `java-test-fixtures`
}

dependencies {
    api(project(":cache-api"))

    testFixturesImplementation("org.junit.jupiter:junit-jupiter:5.11.1")
    testFixturesRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.1")
    testFixturesImplementation("org.awaitility:awaitility:4.2.2")
}

publishing.publications.withType<MavenPublication> {
    pom {
        name.set("Xanthic - Core Module")
        description.set("Xanthic Cache Core dependency")
    }
}
