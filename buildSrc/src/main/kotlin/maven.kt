import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPomDeveloperSpec

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
