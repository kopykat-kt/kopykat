import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}