package buildsrc.conventions

import org.gradle.kotlin.dsl.`java-library`
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.dokka")
}

tasks.dokkaHtml.configure {
    outputDirectory.set(rootDir.resolve("docs/mutable-utils"))
}