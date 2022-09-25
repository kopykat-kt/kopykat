package buildsrc.conventions

plugins {
    `maven-publish`
}

publishing {
    // configure all publications to have the same POM
    publications.withType<MavenPublication>().configureEach {
        pom {
            description.set("Little utilities for more pleasant immutable data in Kotlin")
            url.set("https://kopy.at")
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("kopykat-authors")
                    name.set("The KopyKat authors")
                }
            }
            scm {
                connection.set("scm:git:git://github.com/kopykat-kt/kopykat.git")
                developerConnection.set("scm:git:ssh://git@github.com/kopykat-kt/kopykat.git")
                url.set("https://github.com/kopykat-kt/kopykat")
            }
        }
    }
}

plugins.withType<JavaPlugin>().configureEach {
    // only create a 'java' publication when the JavaPlugin is present
    publishing {
        publications {
            register<MavenPublication>("maven") {
                from(components["java"])
            }
        }
    }
}
