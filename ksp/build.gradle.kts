plugins {
    buildsrc.conventions.`kotlin-jvm`
    buildsrc.conventions.`maven-publish`
}

dependencies {
    implementation(libs.ksp)
    implementation(projects.utils.kotlinPoet)

    testImplementation(projects.utils.compilation)
    testImplementation(kotlin("test"))

    testRuntimeOnly(projects.ksp)
}
