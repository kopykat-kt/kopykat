plugins {
    buildsrc.conventions.`kotlin-jvm`
    buildsrc.conventions.`maven-publish`
    buildsrc.conventions.dokka
}

dependencies {
    testImplementation(libs.kotest.assertions.core)
    testImplementation(kotlin("test"))
}

dependencies {
    testImplementation(libs.kotest.assertions.core)
    testImplementation(kotlin("test"))
}
