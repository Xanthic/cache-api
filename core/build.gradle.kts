plugins {
    `java-test-fixtures`
}

dependencies {
    api(project(":cache-api"))

    testFixturesImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testFixturesRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testFixturesImplementation("org.awaitility:awaitility:4.2.0")
}
