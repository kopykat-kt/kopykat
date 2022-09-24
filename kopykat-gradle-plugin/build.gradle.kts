plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("com.gradle.plugin-publish")
    buildsrc.conventions.`maven-publish`
}

dependencies {
    implementation(libs.gradlePlugin.kotlinJvm)
    implementation(libs.gradlePlugin.ksp)
}

gradlePlugin {
    val kopyKatGradlePlugin by plugins.creating {
        id = "fp.serrano.kopykat"
        implementationClass = "fp.serrano.kopykat.KopyKatPlugin"
        displayName = "KopyKat"
        description = "Little utilities for more pleasant immutable data in Kotlin"
    }

    // Manual re-name of the ID, because JitPack renames the package, which means the
    // auto-generated Gradle plugin marker doesn't match.
    // https://docs.gradle.org/current/userguide/plugins.html#sec:plugin_markers
    // As a quick fix, create another plugin with an ID that will match the JitPack package.
    val kopyKatGradlePluginJitpack by plugins.creating {
        id = "com.github.aSemy.kopykat-gradle-plugin"
        description = kopyKatGradlePlugin.description
        displayName = kopyKatGradlePlugin.displayName
        implementationClass = kopyKatGradlePlugin.implementationClass
    }
}

pluginBundle {
    website = "https://github.com/kopykat-kt/kopykat"
    vcsUrl = "https://github.com/kopykat-kt/kopykat.git"
    tags = listOf("kotlin", "data class", "immutable", "ksp")
}
