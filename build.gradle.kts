allprojects {
  repositories {
    mavenCentral()
  }

  group = "com.github.serras.kopykat"
  version = "0.1"
}

subprojects {
  apply(plugin = "java-library")
  apply(plugin = "maven-publish")

  configure<PublishingExtension> {
    publications {
      create<MavenPublication>("maven") {
        groupId = project.group as String
        artifactId = project.name
        version = project.version as String
        from(components["java"])

        pom {
          description.set("Little utilities for more pleasant immutable data in Kotlin")
          url.set("https://github.com/serras/kopykat")
          licenses {

          }
          licenses {
            license {
              name.set("The Apache License, Version 2.0")
              url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
          }
          developers {
            developer {
              id.set("kopykat-authors")
              name.set("The KopyKat authors")
            }
          }
          scm {
            connection.set("scm:git:git://github.com/serras/kopykat.git")
            developerConnection.set("scm:git:ssh://git@github.com/serras/kopykat.git")
            url.set("https://github.com/serras/kopykat")
          }
        }
      }
    }
  }
}
