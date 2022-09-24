package buildsrc.conventions

plugins {
    base
}

description = "Common config that can be used in all projects"

if (project != rootProject) {
    project.group = rootProject.group
    project.version = rootProject.version
}
