plugins {
    `java-test-fixtures`
}

dependencies {
    api(project(":cache-api"))

    testFixturesImplementation("org.junit.jupiter:junit-jupiter:5.12.0")
    testFixturesRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.12.0")
    testFixturesImplementation("org.awaitility:awaitility:4.3.0")
}

publishing.publications.withType<MavenPublication> {
    pom {
        name.set("Xanthic - Core Module")
        description.set("Xanthic Cache Core dependency")
    }
}
