enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "transformative"

include("transformative-ksp")
project(":transformative-ksp").projectDir = file("ksp")

include(":utils:compilation")
include(":utils:kotlin-poet")
