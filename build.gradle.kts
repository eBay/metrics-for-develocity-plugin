import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    `embedded-kotlin`
    embeddedKotlin("plugin.serialization")
    alias(libs.plugins.openapi.generator)
    alias(libs.plugins.gradle.pluginPublish)
}

group = "com.ebay.plugins"

gradlePlugin {
    website = "https://github.com/eBay/metrics-for-develocity-plugin"
    vcsUrl = "https://github.com/eBay/metrics-for-develocity-plugin.git"
    plugins {
        create("metricsForDevelocity") {
            id = "com.ebay.metrics-for-develocity"
            implementationClass = "com.ebay.plugins.metrics.develocity.MetricsForDevelocityPlugin"
            displayName = "Metrics for Develocity Plugin"
            description = "Gradle plugin which provides a framework for reporting on Develocity build data"
            tags = listOf(
                "develocity", "analysis", "report"
            )
        }
    }
}

dependencies {
    compileOnly(libs.gradle.develocity)

    implementation(libs.kotlinx.serializationJson)
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinxJson)
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

val openApiFile = "develocity-2024.1-api.yaml"
openApiGenerate {
    generatorName.set("kotlin")
    generateModelDocumentation.set(false)
    // The ignore file doesn't seem to work with the gradle plugin, so this is a way to only get it to generate models.
    modelFilesConstrainedTo.set(listOf(""))
    modelPackage.set("com.ebay.plugins.metrics.develocity.service.model")
    inputSpec.set("$projectDir/openapi/$openApiFile")
    outputDir.set(project.layout.buildDirectory.file("generated/openApi").map { it.asFile.path })
    configFile.set("$projectDir/openapi/config.json")
}

openApiValidate {
    inputSpec.set("$projectDir/openapi/$openApiFile")
}

project.tasks.withType(Test::class.java) {
    useJUnitPlatform()
}

