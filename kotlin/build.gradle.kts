plugins {
    kotlin("jvm") version "1.7.22"
    id("org.jetbrains.dokka") version "1.7.20"
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

publishing.publications.withType<MavenPublication> {
    pom {
        name.set("Xanthic - KTX Module")
        description.set("Xanthic Kotlin Extensions dependency")
    }
}
