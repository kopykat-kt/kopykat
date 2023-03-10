enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "kopykat"

apply(from = "./buildSrc/repositories.settings.gradle.kts")

include(
  ":kopykat-annotations",
  ":kopykat-ksp",
  ":utils:compiletesting",
  ":utils:kotlin-poet",
  ":mutable-utils",
)
