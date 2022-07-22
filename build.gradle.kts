// Plugins
plugins {
    `java-library`
    signing
    `maven-publish`
    id("io.freefair.lombok") version "6.5.0.3" apply false
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }

    group = "io.github.xanthic.cache"
    version = "1.0.0-SNAPSHOT"
}

subprojects {
    apply(plugin = "signing")
    apply(plugin = "maven-publish")

    if (project.name != "cache-bom") {
        apply(plugin = "java-library")
        apply(plugin = "io.freefair.lombok")

        extensions.configure(io.freefair.gradle.plugins.lombok.LombokExtension::class.java) {
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
    } else {
        apply(plugin = "java-platform")
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

        publishing {
            publications {
                create<MavenPublication>("main") {
                    if (project.name == "cache-bom") {
                        from(components["javaPlatform"])
                    } else {
                        from(components["java"])
                    }
                    pom.default()
                }
            }
        }
    }

    signing {
        useGpgCmd()
        sign(publishing.publications["main"])
    }
}
