import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    `embedded-kotlin`
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

metricsForDevelocity {
    // The following could be used to change the time zone used by the plugin:
    // zoneId.set("UTC")

    // An additional filter may also be supplied:
    // develocityQueryFilter.set("project:andr_core tag:Local")
}

project.tasks.withType(Test::class.java) {
    useTestNG()
}