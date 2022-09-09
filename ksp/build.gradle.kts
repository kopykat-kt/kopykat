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
    implementation(projects.utils.kotlinPoet)

    testImplementation(projects.utils.compilation)
    testImplementation(kotlin("test"))

    testRuntimeOnly(projects.transformativeKsp)
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
