plugins {
  buildsrc.conventions.`kopykat-base`
}

group = "at.kopyk"
version = System.getenv("RELEASE_TAG") ?: "1.0-SNAPSHOT"
