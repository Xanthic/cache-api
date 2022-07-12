// Plugins
plugins {
    java
    `java-library`
    id("io.freefair.lombok") version "6.4.3.1"
}

group = "io.github.xanthic"
version = "1.0.0"

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "io.freefair.lombok")

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        withSourcesJar()
        withJavadocJar()
    }

    dependencies {
        // annotations
        compileOnly("org.jetbrains:annotations:23.0.0")

        // tests
        testImplementation(platform("org.junit:junit-bom:5.8.2"))
        testImplementation(group = "org.junit.jupiter", name = "junit-jupiter")
        testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine")

        // logging and tests
        api(group = "org.slf4j", name = "slf4j-api", version = "1.7.36")
        testImplementation(group = "org.slf4j", name = "slf4j-simple", version = "1.7.36")
    }

    tasks {
        test {
            useJUnitPlatform()
        }
    }
}
