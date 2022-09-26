plugins {
    buildsrc.conventions.`kotlin-jvm`
    buildsrc.conventions.`maven-publish`
}

dependencies {
    implementation(libs.ksp)
    implementation(projects.utils.kotlinPoet)
    implementation(projects.kopykatAnnotations)

    testImplementation(projects.utils.compiletesting)
    testImplementation(kotlin("test"))

    testRuntimeOnly(projects.kopykatKsp)
}
