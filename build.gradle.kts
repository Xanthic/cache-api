// Plugins
plugins {
    signing
    `maven-publish`
    `java-library`
    id("io.freefair.lombok") version "6.4.3.1"
}

subprojects {
    apply(plugin = "signing")
    apply(plugin = "maven-publish")
    apply(plugin = "java-library")
    apply(plugin = "io.freefair.lombok")

    group = "io.github.xanthic.cache"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
        google()
    }

    lombok {
        version.set("1.18.24")
        disableConfig.set(true)
    }

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

    publishing {
        repositories {
            maven {
                name = "maven"
                url = uri(project.mavenRepositoryUrl)
                credentials {
                    username = project.mavenRepositoryUsername
                    password = project.mavenRepositoryPassword
                }
            }
        }
        publications {
            create<MavenPublication>("main") {
                from(components["java"])
                pom.default()
            }
        }
    }

    signing {
        useGpgCmd()
        sign(publishing.publications["main"])
    }

    tasks {
        // compile options
        withType<JavaCompile> {
            options.encoding = "UTF-8"
        }

        // javadoc & delombok
        val delombok by getting(io.freefair.gradle.plugins.lombok.tasks.Delombok::class)
        javadoc {
            dependsOn(delombok)
            source(delombok)
            options {
                title = "${project.name} (v${project.version})"
                windowTitle = "${project.name} (v${project.version})"
                encoding = "UTF-8"
                overview = file("${rootDir}/buildSrc/overview-single.html").absolutePath
                this as StandardJavadocDocletOptions
                // hide javadoc warnings (a lot from delombok)
                addStringOption("Xdoclint:none", "-quiet")
                if (JavaVersion.current().isJava9Compatible) {
                    // javadoc / html5 support
                    addBooleanOption("html5", true)
                }
            }
        }

        // testing
        test {
            useJUnitPlatform()
        }
    }
}

// Extension functions

val Project.mavenRepositoryUrl: String
    get() = System.getenv("MAVEN_REPO_URL") ?: findProperty("maven.repository.url").toString()

val Project.mavenRepositoryUsername: String
    get() = System.getenv("MAVEN_REPO_USERNAME") ?: findProperty("maven.repository.username").toString()

val Project.mavenRepositoryPassword: String
    get() = System.getenv("MAVEN_REPO_PASSWORD") ?: findProperty("maven.repository.password").toString()

fun MavenPom.default() {
    name.set("Xanthic")
    packaging = "jar"
    url.set("https://Xanthic.github.io")
    issueManagement {
        system.set("GitHub")
        url.set("https://github.com/Xanthic/cache-api/issues")
    }
    inceptionYear.set("2022")
    developers { all }
    licenses {
        license {
            name.set("MIT Licence")
            distribution.set("repo")
            url.set("https://opensource.org/licenses/MIT")
        }
    }
    scm {
        connection.set("scm:git:https://github.com/Xanthic/cache-api.git")
        developerConnection.set("scm:git:git@github.com:Xanthic/cache-api.git")
        url.set("https://github.com/Xanthic/cache-api")
    }
}

val MavenPomDeveloperSpec.all: Unit
    get() {
        PhilippHeuer()
        iProdigy()
    }

fun MavenPomDeveloperSpec.PhilippHeuer() {
    developer {
        id.set("PhilippHeuer")
        name.set("Philipp Heuer")
        email.set("git@philippheuer.me")
        roles.addAll("maintainer")
    }
}

fun MavenPomDeveloperSpec.iProdigy() {
    developer {
        id.set("iProdigy")
        name.set("Sidd")
        roles.addAll("maintainer")
    }
}
