enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "kopykat"

include("kopykat-ksp")
project(":kopykat-ksp").projectDir = file("ksp")

include(":utils:compilation")
include(":utils:kotlin-poet")
