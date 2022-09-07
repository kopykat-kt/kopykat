import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    explicitApi()
}

dependencies {
    implementation(libs.kotlin.stdlibJDK8)
    implementation(libs.kotlinPoet)
    implementation(libs.kotlinPoet.ksp)
    implementation(libs.ksp)

    testImplementation(projects.utils.compilation)
    testImplementation(kotlin("test"))

    testRuntimeOnly(projects.transformativeKsp)
    testRuntimeOnly(projects.transformativeTypes)
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
