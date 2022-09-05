import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}