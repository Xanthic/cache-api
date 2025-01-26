import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm") version "2.1.0"
    id("org.jetbrains.dokka") version "2.0.0"
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))

    // Xanthic
    api(project(":cache-core"))
    testImplementation(project(":cache-provider-caffeine"))
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_1_8
    }
}

tasks.kotlinSourcesJar {
    // Workaround for https://youtrack.jetbrains.com/issue/KT-54207/ in order to restore reproducibility
    enabled = false
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
