// Infrastructure layer: PackageManager/Build repositories, the HTTP logs interceptor and the
// Room store. Implements the domain's repository interfaces; knows nothing about presentation.
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android {
    namespace = "pt.tiagodiasduarte.cabitools.sdk.infrastructure"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }
    resourcePrefix = "cabitools_"

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    testOptions {
        unitTests {
            // Robolectric needs the merged resources and manifest.
            isIncludeAndroidResources = true
        }
        // Robolectric on API 36 needs JDK internals that JDK 17+ hides by default.
        unitTests.all { it.jvmArgs("--add-exports=java.base/jdk.internal.access=ALL-UNNAMED") }
    }
}

kotlin {
    explicitApi()
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    autoCorrect = false
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(project(":cabitools-sdk-domain"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)

    // Host apps bring their own OkHttp through Retrofit; the SDK never picks its version.
    compileOnly(libs.okhttp)

    ksp(libs.androidx.room.compiler)

    testImplementation(libs.androidx.junit)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.robolectric)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
}
