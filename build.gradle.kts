// Plugins
plugins {
    `java-library`
    signing
    `maven-publish`
    id("io.freefair.lombok") version "8.6" apply false
    jacoco
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }

    group = "io.github.xanthic.cache"
    version = "0.6.0"
}

subprojects {
    apply(plugin = "signing")
    apply(plugin = "maven-publish")

    if (project.name == "cache-bom") {
        apply(plugin = "java-platform")
    } else {
        apply(plugin = "java-library")
        apply(plugin = "io.freefair.lombok")
        apply(plugin = "jacoco")

        extensions.configure(io.freefair.gradle.plugins.lombok.LombokExtension::class.java) {
            version.set("1.18.30")
            disableConfig.set(true)
        }

        extensions.configure(JacocoPluginExtension::class.java) {
            toolVersion = "0.8.10"
        }

        java {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
            withSourcesJar()
            withJavadocJar()
        }

        dependencies {
            // annotations
            compileOnly("org.jetbrains:annotations:24.1.0")
            testCompileOnly("org.jetbrains:annotations:24.1.0")

            // tests
            testImplementation(platform("org.junit:junit-bom:5.10.3"))
            testImplementation(group = "org.junit.jupiter", name = "junit-jupiter")
            testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine")

            // logging and tests
            api(group = "org.slf4j", name = "slf4j-api", version = "2.0.13")
            testImplementation(group = "org.slf4j", name = "slf4j-simple", version = "2.0.13")
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

            // reproducible builds
            withType<AbstractArchiveTask>().configureEach {
                isPreserveFileTimestamps = false
                isReproducibleFileOrder = true
            }

            // testing
            test {
                useJUnitPlatform()
                finalizedBy(jacocoTestReport)
            }

            jacocoTestReport {
                dependsOn(test)
                reports.xml.required.set(true)
            }
        }
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
        isRequired = false // only sign when credentials are configured
        useGpgCmd()
        sign(publishing.publications["main"])
    }
}
