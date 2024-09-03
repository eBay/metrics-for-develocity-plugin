pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    includeBuild("..")
}

plugins {
    id("com.ebay.metrics-for-develocity")
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
