dependencies {
    api(project(":cache-core"))
    implementation("com.fasterxml.jackson.core:jackson-databind") {
        version {
            require("2.16.0") // imposes a lower bound on acceptable versions
        }
    }
    testImplementation(project(":cache-provider-caffeine"))
}

publishing.publications.withType<MavenPublication> {
    pom {
        name.set("Xanthic - Jackson")
        description.set("Xanthic Cache Jackson Adapter")
    }
}
