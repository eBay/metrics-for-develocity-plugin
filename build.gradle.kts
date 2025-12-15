@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    `embedded-kotlin`
    embeddedKotlin("plugin.serialization")
    alias(libs.plugins.gradle.pluginPublish)
}

group = "com.ebay.plugins"

gradlePlugin {
    website = "https://github.com/eBay/metrics-for-develocity-plugin"
    vcsUrl = "https://github.com/eBay/metrics-for-develocity-plugin.git"
    plugins {
        create("metricsForDevelocity") {
            id = "com.ebay.metrics-for-develocity"
            implementationClass = "com.ebay.plugins.metrics.develocity.MetricsForDevelocitySettingsPlugin"
            displayName = "Metrics for Develocity Plugin"
            description = "Gradle plugin which provides a framework for reporting on Develocity build data"
            tags = listOf(
                "develocity", "analysis", "report"
            )
        }
    }
}

dependencies {
    api(libs.develocityApi)

    implementation(libs.gradle.develocity)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serializationJson)
    implementation(libs.pluginLib.ebay.graphAnalytics)

    testImplementation(libs.test.hamcrest)
    testImplementation(libs.test.testng)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

project.tasks.withType(KotlinJvmCompile::class.java) {
    compilerOptions {
        allWarningsAsErrors.set(true)
        freeCompilerArgs.addAll(listOf("-Xjvm-default=all", "-opt-in=kotlin.RequiresOptIn"))
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

project.tasks.withType(Test::class.java) {
    useTestNG()
}

