plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    buildsrc.conventions.`maven-publish`
}

dependencies {
    api(projects.kopykatGradlePlugin)
}

// Manual re-name of the ID + group, because JitPack renames the package, which means the
// auto-generated Gradle plugin marker doesn't match.
// https://docs.gradle.org/current/userguide/plugins.html#sec:plugin_markers
// As a quick fix, rename the group and create another plugin with a suitable ID

group = "com.github.aSemy.kopykat-gradle-plugin"

gradlePlugin {
    val kopyKatGradlePluginJitpack by plugins.creating {
        id = project.group.toString()
        implementationClass = "fp.serrano.kopykat.KopyKatPlugin"
        displayName = "KopyKat"
        description = "Little utilities for more pleasant immutable data in Kotlin"
    }
}

pluginBundle {
    website = "https://github.com/kopykat-kt/kopykat"
    vcsUrl = "https://github.com/kopykat-kt/kopykat.git"
    tags = listOf("kotlin", "data class", "immutable", "ksp")
}

// might fix JitPack compatibility?
publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
