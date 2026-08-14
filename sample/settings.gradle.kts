pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    includeBuild("..")
}

plugins {
    id("com.ebay.graph-analytics") version("1.2.0") // Keep in sync with version catalogs
    id("com.ebay.metrics-for-develocity")
    id("com.gradle.develocity") version("4.5.0")
    id("com.gradle.common-custom-user-data-gradle-plugin") version("2.8.0")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "metrics-for-develocity-sample"

val isCI = System.getenv("CI") != null

develocity {
    server = "https://community.develocity.cloud"
    projectId = "ebay"
    buildScan {
        uploadInBackground = !isCI
        publishing.onlyIf { it.isAuthenticated }
        obfuscation {
            ipAddresses { addresses -> addresses.map { _ -> "0.0.0.0" } }
        }
    }
}

buildCache {
    local {
        isEnabled = true
    }

    remote(develocity.buildCache) {
        isEnabled = true
        // Check access key presence to avoid build cache errors on PR builds when access key is not present
        val accessKey = System.getenv("DEVELOCITY_ACCESS_KEY")
        isPush = isCI && accessKey != null
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":subproj1")
