enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "kopykat"

apply(from = "./buildSrc/repositories.settings.gradle.kts")

include(
    ":kopykat-ksp",
    ":kopykat-gradle-plugin",
    ":utils:compiletesting",
    ":utils:kotlin-poet",
)
