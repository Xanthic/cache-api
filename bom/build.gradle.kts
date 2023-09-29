plugins {
    `java-platform`
}

val projectModules = rootProject.subprojects.filter { it.name != "cache-bom" }.map { project(":${it.name}") }

dependencies {
    constraints {
        projectModules.forEach(::api)
    }
}

publishing.publications.withType<MavenPublication> {
    pom {
        name.set("Xanthic - BOM Platform")
        description.set("Xanthic Build of Materials dependency constraints")
        packaging = "pom"
    }
}
