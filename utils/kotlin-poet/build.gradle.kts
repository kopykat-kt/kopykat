import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    explicitApi()
}

dependencies {
    implementation(libs.ksp)
    api(libs.kotlinPoet)
    api(libs.kotlinPoet.ksp)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
