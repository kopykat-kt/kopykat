package buildsrc.conventions

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("buildsrc.conventions.kopykat-base")
    kotlin("jvm")
    `java-library`
    id("org.jlleitschuh.gradle.ktlint")
}

dependencies {
    implementation(platform(kotlin("bom")))
}

kotlin {
    explicitApi()
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of("11"))
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "1.8"
}

java {
    withSourcesJar()
    withJavadocJar()
}
