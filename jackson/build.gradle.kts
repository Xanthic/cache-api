dependencies {
    api(project(":cache-core"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2") // TODO: 2.16.0
}

publishing.publications.withType<MavenPublication> {
    pom {
        name.set("Xanthic - Jackson")
        description.set("Xanthic Cache Jackson Adapter")
    }
}
