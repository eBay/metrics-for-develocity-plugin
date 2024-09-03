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

project.tasks.withType(Test::class.java) {
    useTestNG()
}
