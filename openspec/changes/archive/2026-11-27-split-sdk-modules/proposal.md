# Proposal

## Why

All SDK code lives in one `:sdk` module, so the clean-architecture layers are only folders: nothing stops presentation code from reaching into the database, or Android code from creeping into the domain. Splitting the layers into Gradle modules makes the build enforce the boundaries, and keeps each layer small enough to change on its own as more tools are added.

## What Changes

- Split `:sdk` into four modules, one per layer:
  - `:cabitools-sdk-domain` (pure Kotlin/JVM): models, `ContentTypes`, repository interfaces and use cases.
  - `:cabitools-sdk-infrastructure` (Android library): PackageManager/`Build` repositories, the HTTP logs interceptor and capture code, and the Room store. Depends on domain.
  - `:cabitools-sdk-presentation` (Android library): Compose screens, ViewModels, Navigation 3 and resources. Depends on domain.
  - `:cabitools-sdk-application` (Android library): the public API host apps use (`Cabitools.open`, `Cabitools.httpInterceptor`, `HttpLogsConfig`, `Retention`, `HttpLogsDsl`), the manual DI (`SdkContainer`) and `CabitoolsActivity`. Depends on the other three with `implementation`.
- **BREAKING** Remove `:sdk`: host apps depend on `:cabitools-sdk-application` instead. The public API keeps its package (`pt.tiagodiasduarte.cabitools.sdk`), so host imports and calls don't change; only dependency lines and composite build substitutions do.
- **BREAKING** Rename `:sdk-no-op` to `:cabitools-sdk-application-no-op`, the production mirror of the application module.
- Remove the two dependencies that point the wrong way: ViewModel factories and the navigation host take their dependencies (through a `PresentationDependencies` interface) instead of `SdkContainer`, and infrastructure takes plain values (header name sets, retention in milliseconds) instead of the public `HttpLogsConfig`/`Retention` types.
- Declarations used across modules become `public` in their module (`explicitApi()` everywhere); everything else stays `internal`.
- Coverage is merged across the four modules and still verified at 90%, now by `:cabitools-sdk-application:koverVerify`; `checkNoOpApi` compares the application module with its no-op. Tests move with their code.

No behavior changes, and the public API is unchanged.

Out of scope: publishing the modules separately, and updating host apps (android-interview-challenge must switch its dependency and substitution to the new module names separately).

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
<!-- None: no observable behavior changes. The change sets skip_specs: true. -->

## Impact

- **Host apps:** `debugImplementation(":cabitools-sdk-application")` and `releaseImplementation(":cabitools-sdk-application-no-op")` (or the matching composite build substitutions) replace `:sdk` and `:sdk-no-op`. Imports and calls stay the same.
- **Build:** four modules replace `:sdk` in `settings.gradle.kts`, and the no-op is renamed; the Kotlin JVM plugin (pinned to AGP's Kotlin 2.2.10) and `kotlinx-coroutines-core` for domain; Room/KSP move to infrastructure, Compose and Navigation 3 to presentation; CI and `checkNoOpApi` point at the application module.
- **Code:** every SDK source file moves to a layer module; `data` becomes `infrastructure`; resources move to presentation (its `R` class gets the presentation namespace).
- **Public API:** unchanged, now declared in `:cabitools-sdk-application`. Types that become `public` inside domain, infrastructure and presentation are not on host compile classpaths, because the application module depends on them with `implementation`.
