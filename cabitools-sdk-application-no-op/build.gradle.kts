plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.detekt)
}

// Production stand-in for :sdk. Same public API (same package and declarations), no behavior:
// no recording, no database, no screens. Host apps use it in production builds only.
android {
    namespace = "pt.tiagodiasduarte.cabitools.sdk.noop"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }
    resourcePrefix = "cabitools_"

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    testOptions {
        // The no-op only needs a Context instance, never its behavior.
        unitTests.isReturnDefaultValues = true
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

dependencies {
    // Host apps bring their own OkHttp through Retrofit; the no-op never picks its version.
    compileOnly(libs.okhttp)

    testImplementation(libs.junit)
    testImplementation(libs.okhttp)
    testImplementation(libs.okhttp.mockwebserver)
}
