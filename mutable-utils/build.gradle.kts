plugins {
  buildsrc.conventions.`kotlin-jvm`
  buildsrc.conventions.`maven-publish`
}

dependencies {
  testImplementation(libs.kotest.assertions.core)
  testImplementation(kotlin("test"))
}
