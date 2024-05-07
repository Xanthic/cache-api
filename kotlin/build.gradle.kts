plugins {
    kotlin("jvm") version "1.9.24"
    id("org.jetbrains.dokka") version "1.9.20"
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))

    // Xanthic
    api(project(":cache-core"))
    testImplementation(project(":cache-provider-caffeine"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
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
