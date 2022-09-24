enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "kopykat"

apply(from = "./buildSrc/repositories.settings.gradle.kts")

include(
    ":ksp",
    ":kopykat-gradle-plugin",
    ":utils:compilation",
    ":utils:kotlin-poet",
)
