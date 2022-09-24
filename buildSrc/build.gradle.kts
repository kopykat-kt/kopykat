import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    kotlin("jvm") version embeddedKotlinVersion
}

val gradleJvmTarget = "11"
val gradleKotlinTarget = "1.6"

val kotlinVersion = "1.7.10"

dependencies {
    implementation(platform(libs.kotlin.bom))

    // Set the *Maven coordinates* of Gradle plugins here.
    // This should be the only place where Gradle plugins versions are defined.

    implementation(libs.gradlePlugin.kotlinJvm)
    implementation(libs.gradlePlugin.ksp)
    implementation(libs.gradlePlugin.pluginPublish)
}


tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = gradleJvmTarget
        apiVersion = gradleKotlinTarget
        languageVersion = gradleKotlinTarget
    }
}


kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(gradleJvmTarget))
    }
}


kotlinDslPluginOptions {
    jvmTarget.set(gradleJvmTarget)
}
