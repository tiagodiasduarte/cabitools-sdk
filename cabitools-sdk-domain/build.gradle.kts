import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Domain layer: models, repository interfaces and use cases. Pure Kotlin, so the build
// itself forbids Android dependencies here.
plugins {
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kover)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    explicitApi()
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    autoCorrect = false
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
}

dependencies {
    // HttpLogsRepository exposes a Flow.
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
}
