dependencies {
    api(project(":cache-core"))

    implementation("com.github.ben-manes.caffeine:caffeine:3.1.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
