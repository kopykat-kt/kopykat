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