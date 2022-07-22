plugins {
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.dokka") version "1.7.10"
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))

    // Xanthic
    api(project(":cache-core"))
    testImplementation(project(":cache-provider-caffeine"))
}

tasks.javadocJar {
    from(tasks.dokkaJavadoc)
}
