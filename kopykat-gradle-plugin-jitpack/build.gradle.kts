plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    buildsrc.conventions.`maven-publish`
}

dependencies {
    api(projects.kopykatGradlePlugin)
}

project.group = "com.github.aSemy.kopykat"

gradlePlugin {
    val kopyKatGradlePluginJitpack by plugins.creating {
        id = "com.github.aSemy.kopykat"
        implementationClass = "fp.serrano.kopykat.KopyKatPlugin"
        displayName = "KopyKat"
        description = "Little utilities for more pleasant immutable data in Kotlin"
    }
}

// might fix JitPack compatibility?
publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
