enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "transformative"

include("transformative-types")
project(":transformative-types").projectDir = file("types")

include("transformative-ksp")
project(":transformative-ksp").projectDir = file("ksp")

include("mutable-copy")

include(":utils:compilation")
include(":utils:kotlin-poet")
