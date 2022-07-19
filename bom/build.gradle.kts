plugins {
    id("signing")
    id("maven-publish")
    id("io.github.gradlebom.generator-plugin") version "1.0.0.Final"
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
            artifactId = "cache-bom"
            pom.default()
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["main"])
}
