plugins {
    buildsrc.conventions.`kotlin-jvm`
    buildsrc.conventions.`maven-publish`
}

dependencies {
    implementation(libs.ksp)
    api(libs.kotlinPoet)
    api(libs.kotlinPoet.ksp)
}
