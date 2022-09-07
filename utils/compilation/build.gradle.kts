import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    explicitApi()
}

dependencies {
    implementation(kotlin("test"))
    implementation(libs.assertj)
    implementation(libs.classgraph)
    implementation(libs.kotlinCompileTesting) {
        exclude(
            group = libs.classgraph.get().module.group,
            module = libs.classgraph.get().module.name
        )
        exclude(
            group = libs.kotlin.stdlibJDK8.get().module.group,
            module = libs.kotlin.stdlibJDK8.get().module.name
        )
    }
    implementation(libs.kotlinCompileTestingKsp)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
