pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    includeBuild("..")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "metrics-for-develocity-sample"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":subproj1")
