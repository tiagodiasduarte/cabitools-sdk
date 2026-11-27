# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project state

Cabitools (named after *cabidela*) is an in-app debug and QA tools screen for my own apps. The SDK is split into one module per clean-architecture layer, plus a no-op and a sample:

- `:cabitools-sdk-application` (`com.android.library`, namespace `pt.tiagodiasduarte.cabitools.sdk`) is the module host apps depend on and the composition root. It holds the public API, the `Cabitools` object (`explicitApi()` is on): `Cabitools.open(context)` launches the internal, non-exported `CabitoolsActivity`, and `Cabitools.httpInterceptor(context) { ... }` returns the HTTP logs OkHttp interceptor, configured with `HttpLogsConfig`/`Retention`. It also holds the manual DI (`di/SdkContainer`, no Hilt so host apps aren't forced to adopt it), `CabitoolsActivity` and its window themes, and depends on the three layer modules with `implementation`. See `cabitools-sdk-application/README.md`.
- `:cabitools-sdk-domain` (pure Kotlin/JVM, `org.jetbrains.kotlin.jvm`) holds models, `ContentTypes`, repository interfaces and use cases. It must never depend on Android.
- `:cabitools-sdk-infrastructure` (namespace `…sdk.infrastructure`) implements the domain repositories: PackageManager/`Build`, the HTTP logs interceptor and capture code, and the Room store (schema exported to its `schemas/`). Its public surface is only `AppInfoRepositoryImpl`, `DeviceInfoRepositoryImpl`, `HeaderRules` and the `HttpLogs` entry point; the store, interceptor, DAO and entities are `internal`.
- `:cabitools-sdk-presentation` (namespace `…sdk.presentation`, owns the strings and icons) holds the Compose screens, ViewModels and Navigation 3. The screen renders an ordered list of sections, currently Application, Device and Logs; Logs opens the HTTP logs screens. It gets its dependencies through `PresentationDependencies` (implemented by `SdkContainer`) and must never depend on infrastructure.
- `:cabitools-sdk-application-no-op` (namespace `…sdk.noop`) has the same public API as `:cabitools-sdk-application` (same package and declarations) and does nothing. Host apps use it in production builds. Any public API change must be made in both modules; `./gradlew checkNoOpApi` fails when they differ.
- `:app` (namespace and application id `pt.tiagodiasduarte.cabitools.sample`, "Cabitools Sample") is a sample host: a Compose `MainActivity` that opens Cabitools and makes HTTP calls through the interceptor. Debug and the minified `qa` build type use `:cabitools-sdk-application`; release uses `:cabitools-sdk-application-no-op`.

Dependency rules: domain ← infrastructure, domain ← presentation, and application → all three. Declarations used by another module are `public` in their module; everything else stays `internal`. All Android modules use the `cabitools_` resource prefix (`resourcePrefix`). Model-to-model mapping functions live in each module's `mapper` package (for example `…sdk.presentation.mapper`), one file per source model, as `internal` extension functions.

## Commands

```bash
./gradlew assembleDebug                 # build debug APK
./gradlew installDebug                  # install on connected device/emulator
./gradlew test                          # JVM unit tests of every module, incl. Robolectric/Compose UI tests
./gradlew connectedAndroidTest          # instrumented tests (application, infrastructure and app modules), needs a device
./gradlew lint                          # Android lint
./gradlew detekt                        # static analysis (config/detekt/detekt.yml)
./gradlew :cabitools-sdk-application:koverHtmlReport :cabitools-sdk-application:koverVerify   # merged SDK coverage and the 90% minimum
./gradlew checkNoOpApi                  # application module and its no-op must expose the same public API

# single test class / method
./gradlew :cabitools-sdk-presentation:testDebugUnitTest --tests "pt.tiagodiasduarte.cabitools.sdk.presentation.SettingsViewModelTest"
./gradlew :cabitools-sdk-domain:test --tests "pt.tiagodiasduarte.cabitools.sdk.domain.usecase.GetDeviceInfoUseCaseTest"
./gradlew :cabitools-sdk-application:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=pt.tiagodiasduarte.cabitools.sdk.CabitoolsTest
```

CI (`.github/workflows/ci.yml`) runs on PRs into `develop` and `main`: build, unit tests with the coverage check, lint, detekt and `checkNoOpApi`. The `🏁 Finish` job aggregates the result.

Framework-dependent SDK code (`Cabitools.open`, PackageManager/`Build` access, the Compose screen) is unit tested with Robolectric so Kover can measure it; Kover excludes the Activity, `di` and `theme` packages and `@Composable`/`@Preview` functions.

## Build setup

- AGP 9.x with its built-in Kotlin support: there is no separate `org.jetbrains.kotlin.android` plugin. Don't add one unless the AGP version requires it. The Kotlin Compose compiler plugin (`libs.plugins.kotlin.compose`) is pinned to the Kotlin version AGP bundles (check with `./gradlew buildEnvironment`).
- Dependencies and plugins are declared in the version catalog `gradle/libs.versions.toml`; reference them as `libs.*` and don't hardcode coordinates in build scripts.
- `compileSdk` 37 (minor API level 1, required by current AndroidX/Compose), `targetSdk` 36, `minSdk` 24, Java 11 source/target, in both modules.
- Configuration cache is enabled (`gradle.properties`), so build logic must be configuration-cache compatible.
- `settings.gradle.kts` uses `FAIL_ON_PROJECT_REPOS`: add repositories there, not in module build files.
- R8 keep rules go in `<module>/src/main/keepRules/` (AGP merges every file in that directory), not in a `proguard-rules.pro`. Release optimization is currently disabled (`optimization { enable = false }`).
- UI is Jetpack Compose with Material 3 (Compose BOM). There's no AppCompat or Material Components: the XML themes are platform `android:Theme.Material.*.NoActionBar` window themes only. The SDK's `CabitoolsTheme` follows the system dark mode and uses dynamic color on API 31+.

## Local skills

`skills/` holds Android guidance skills (architecture/Hilt, data layer, ViewModel, coroutines, Retrofit, Compose UI, Navigation 3, testing, Gradle convention plugins, build performance, emulator automation scripts, migrations). They are reference material, not part of the build. Read the relevant `skills/<category>/<name>/SKILL.md` before doing work in that area.

## OpenSpec

The project uses OpenSpec (spec-driven schema) for planning changes. Specs live in `openspec/specs/`, in-progress changes in `openspec/changes/` (archived ones in `openspec/changes/archive/`), and project-level constraints for artifacts go in `openspec/config.yaml`. The workflow skills (`openspec-propose`, `-explore`, `-apply-change`, `-update-change`, `-sync-specs`, `-archive-change`) are in `.claude/skills/`, mirrored in `.agents/skills/`.
