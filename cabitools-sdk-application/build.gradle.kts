import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kover)
}

android {
    namespace = "pt.tiagodiasduarte.cabitools.sdk"
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
    buildFeatures {
        compose = true
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

kover {
    reports {
        filters {
            excludes {
                annotatedBy("androidx.compose.ui.tooling.preview.Preview")
                annotatedBy("androidx.compose.runtime.Composable")

                classes(
                    // Generated code
                    "*ComposableSingletons*",
                    "*_Impl",
                    "*_Impl$*",
                    "pt.tiagodiasduarte.cabitools.sdk.BuildConfig",

                    // Activity, DI and theme
                    "pt.tiagodiasduarte.cabitools.sdk.CabitoolsActivity*",
                    "pt.tiagodiasduarte.cabitools.sdk.di.*",
                    "pt.tiagodiasduarte.cabitools.sdk.presentation.theme.*",
                )
            }
        }

        verify {
            rule("line-coverage") {
                minBound(
                    minValue = 90,
                    coverageUnits = CoverageUnit.LINE,
                    aggregationForGroup = AggregationType.COVERED_PERCENTAGE
                )
            }
            rule("branch-coverage") {
                minBound(
                    minValue = 90,
                    coverageUnits = CoverageUnit.BRANCH,
                    aggregationForGroup = AggregationType.COVERED_PERCENTAGE
                )
            }
        }
    }
}

dependencies {
    implementation(project(":cabitools-sdk-domain"))
    implementation(project(":cabitools-sdk-infrastructure"))
    implementation(project(":cabitools-sdk-presentation"))
    // CabitoolsActivity sets the Compose content.
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)

    // Host apps bring their own OkHttp through Retrofit; the SDK never picks its version.
    compileOnly(libs.okhttp)

    testImplementation(libs.androidx.junit)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.robolectric)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}

// Merge the layer modules' coverage into this module's report and 90% verification.
dependencies {
    kover(project(":cabitools-sdk-domain"))
    kover(project(":cabitools-sdk-infrastructure"))
    kover(project(":cabitools-sdk-presentation"))
}
