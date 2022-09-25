plugins {
    buildsrc.conventions.`kotlin-jvm`
    buildsrc.conventions.`maven-publish`
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
