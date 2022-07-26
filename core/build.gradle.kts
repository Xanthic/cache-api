plugins {
    `java-test-fixtures`
}

dependencies {
    api(project(":cache-api"))

    testFixturesImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testFixturesRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    testFixturesImplementation("org.awaitility:awaitility:4.2.0")
}
