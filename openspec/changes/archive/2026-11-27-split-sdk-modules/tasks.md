# Tasks

## 1. Module scaffolding

- [x] 1.1 Add the Kotlin JVM plugin (pinned to the catalog `kotlin` version) and `kotlinx-coroutines-core` to the catalog, sorted per the Gradle conventions, and create empty `:cabitools-sdk-domain` (JVM, Java 11, `explicitApi()`, detekt, Kover), `:cabitools-sdk-infrastructure` and `:cabitools-sdk-presentation` (Android library, namespaces `…sdk.infrastructure`/`…sdk.presentation`, `resourcePrefix = "cabitools_"`, `explicitApi()`, detekt, Kover) modules, included in `settings.gradle.kts`. Verify with `./gradlew assemble --configuration-cache`
- [x] 1.2 Rename `sdk/` to `cabitools-sdk-application/` and `sdk-no-op/` to `cabitools-sdk-application-no-op/` with `git mv`, update `settings.gradle.kts`, the sample app dependencies, `checkNoOpApi` and CI, and make the application module depend on the three layer modules with `implementation` and merge their coverage. Verify that `./gradlew assembleDebug :cabitools-sdk-application:koverVerify checkNoOpApi` passes with the layer code not yet moved

## 2. Domain

- [x] 2.1 Move `domain/*` (models, `ContentTypes`, repository interfaces, use cases) with its tests to `:cabitools-sdk-domain`, making them `public`. Verify that `:cabitools-sdk-domain:test` passes and the module has no Android dependency

## 3. Infrastructure

- [x] 3.1 Move the `HttpLogsConfig` to `HeaderRules` mapping into the application module and replace `HttpLogsStore.retention: Retention` with `retentionMillis: Long`, updating their tests. Verify that `:cabitools-sdk-application:testDebugUnitTest` passes
- [x] 3.2 Move `data/*` (repositories, `http`, `local`, Room schema) with its unit and instrumented tests to `:cabitools-sdk-infrastructure` in the `…sdk.infrastructure` packages, with Room/KSP there, and make the declarations listed in design.md `public`. Verify that `:cabitools-sdk-infrastructure:testDebugUnitTest` passes and the Room schema is exported from the new module

## 4. Presentation

- [x] 4.1 Add `PresentationDependencies`, make `CabitoolsNavHost` and the ViewModel factories take dependencies instead of `SdkContainer`, and implement the interface in `SdkContainer`. Verify that `:cabitools-sdk-application:testDebugUnitTest` passes
- [x] 4.2 Move `presentation/*` (except `CabitoolsActivity`), resources and tests to `:cabitools-sdk-presentation` with Compose and Navigation 3, using its `R` class and fake `PresentationDependencies` in tests; move `CabitoolsActivity` into the application module's root package. Verify that `:cabitools-sdk-presentation:testDebugUnitTest` passes and the merged manifest of `:app` declares `CabitoolsActivity` with `exported=false`

## 5. Application module, docs and checks

- [x] 5.1 Leave only the public API, `SdkContainer` and `CabitoolsActivity` in the application module, with `HttpLogsConfigTest`, `CabitoolsOpenTest`, `CabitoolsHttpInterceptorTest`, a new `SdkContainer` wiring test, a header rules mapping test and the instrumented `CabitoolsTest`; move Kover exclusions to the modules that own the code. Verify that `./gradlew :cabitools-sdk-application:koverVerify` passes at 90% line and branch coverage
- [x] 5.2 Update `CLAUDE.md` (modules, dependency rules, commands) and `sdk/README.md` (now in the application module: integration with the new module names, architecture). Verify that the documented module graph and dependency lines match `settings.gradle.kts` and the sample app
- [x] 5.3 Run `./gradlew assembleDebug test :cabitools-sdk-application:koverVerify lint detekt checkNoOpApi` and the instrumented tests on an emulator, and verify that all pass and the public API dump is unchanged

## Workflow follow-up

- Open the PR from `refactor/split-sdk-modules` into `develop`, and archive the change as its last commit.
- Switch android-interview-challenge to `:cabitools-sdk-application` and `:cabitools-sdk-application-no-op` separately.
