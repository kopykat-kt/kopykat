package buildsrc.conventions

import java.net.URI

plugins {
    `maven-publish`
    signing
}

publishing {
    // configure all publications to have the same POM
    publications.withType<MavenPublication>().configureEach {
        pom {
            description.set("Little utilities for more pleasant immutable data in Kotlin")
            url.set("https://kopy.at")
            name.set("KopyKat")
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

    repositories {
        maven {
            name = "OSSRH"
            url = URI("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
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

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["maven"])
}
