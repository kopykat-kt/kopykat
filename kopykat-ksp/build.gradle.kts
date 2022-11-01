plugins {
    buildsrc.conventions.`kotlin-jvm`
    buildsrc.conventions.`maven-publish`
}

dependencies {
    implementation(libs.ksp)
    implementation(projects.utils.kotlinPoet)
    implementation(projects.kopykatAnnotations)
    implementation(libs.apache.commons.io)

    testImplementation(projects.utils.compiletesting)
    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter.params)

    testRuntimeOnly(projects.kopykatKsp)
}
