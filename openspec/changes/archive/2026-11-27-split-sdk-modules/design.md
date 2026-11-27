# Design

## Context

See proposal.md - Why. Today `:sdk` holds everything under `pt.tiagodiasduarte.cabitools.sdk`, in the packages `domain` (models, repository interfaces, use cases), `data` (repositories, `http` capture, `local` Room store), `presentation` (Compose, ViewModels, navigation, theme), `di` (`SdkContainer`) and the root package (public `Cabitools`, `HttpLogsConfig`, `Retention`, `HttpLogsDsl`). `:sdk-no-op` mirrors the public API. Mapping each file's imports shows two dependencies that point the wrong way for a layered split:

1. Presentation depends on DI: the ViewModel factories take `SdkContainer`, and `CabitoolsActivity` creates it.
2. Infrastructure depends on the public API types: `HeaderRules.from(HttpLogsConfig)` and `HttpLogsStore.retention: Retention`.

Everything else already points inward (towards domain).

## Goals / Non-Goals

**Goals:**
- The build enforces the layers: domain can't use Android, infrastructure and presentation can't use each other, and nothing depends on the application module.
- The public API (package, types, signatures) doesn't change.
- The same checks keep passing: tests, the 90% coverage minimum (merged), lint, detekt, `checkNoOpApi`.

**Non-Goals:**
- Splitting by feature (app info, device info, HTTP logs) instead of by layer.
- Publishing each module as its own artifact.
- A compatibility `:sdk` wrapper: only my own apps use the SDK.

## Decisions

### Module graph

```
 host app ──> :cabitools-sdk-application   (public API, SdkContainer, CabitoolsActivity)
                 |              |               |
                 v              v               v
   :cabitools-sdk-presentation  :cabitools-sdk-infrastructure
          (Android)                   (Android)
                 |                        |
                 +----------+-------------+
                            v
                  :cabitools-sdk-domain  (JVM)

 production: host app ──> :cabitools-sdk-application-no-op  (same public API, no behavior)
```

- `domain` depends only on `kotlinx-coroutines-core` (`HttpLogsRepository` exposes a `Flow`).
- `infrastructure` and `presentation` depend on `domain`, never on each other.
- `application` depends on all three with `implementation`, so their public types never reach host compile classpaths. It is the composition root: the only module that knows every layer.

Alternative: a separate pure-Kotlin application layer for use cases, under a facade. Rejected in favor of the application module being the host-facing API; use cases stay in domain, so presentation can use them without depending on the module above it.

### What goes where

| Today (`:sdk`) | Module | Package |
|---|---|---|
| `domain/model/*`, `domain/repository/*`, `domain/usecase/*` | domain | unchanged (`…sdk.domain.model`, `.repository`, `.usecase`) |
| `data/*` (repositories, `http`, `local`) | infrastructure | `…sdk.infrastructure`, `…sdk.infrastructure.http`, `…sdk.infrastructure.local` |
| `presentation/*` except the Activity | presentation | unchanged (`…sdk.presentation…`) |
| `Cabitools`, `HttpLogsConfig`, `Retention`, `HttpLogsDsl`, `di/SdkContainer`, `presentation/CabitoolsActivity` | application | root and `…sdk.di` unchanged; the Activity moves to `…sdk` |
| `:sdk-no-op` | application-no-op | unchanged |

The application module keeps `:sdk`'s namespace (`pt.tiagodiasduarte.cabitools.sdk`); the others get `…sdk.infrastructure` and `…sdk.presentation`, and the no-op keeps `…sdk.noop`. Presentation code then imports `pt.tiagodiasduarte.cabitools.sdk.presentation.R`. All Android modules keep `resourcePrefix = "cabitools_"`; presentation holds the resources, including the window theme the Activity uses.

`sdk/` is renamed to `cabitools-sdk-application/` with `git mv`, so the public API, DI and Activity keep their history; the layer code then moves out of it. `sdk-no-op/` is renamed to `cabitools-sdk-application-no-op/`.

### Fixing the two wrong-way dependencies

**Presentation without DI.** Presentation declares what its screens need:

```kotlin
public interface PresentationDependencies {
    public val getAppInfo: GetAppInfoUseCase
    public val getDeviceInfo: GetDeviceInfoUseCase
    public val httpLogsRepository: HttpLogsRepository
}

@Composable
public fun CabitoolsNavHost(dependencies: PresentationDependencies)
```

ViewModel factories take those dependencies directly (for example `LogsViewModel.factory(repository)`). `SdkContainer` implements `PresentationDependencies`. `CabitoolsActivity` moves to the application module, next to the other Android entry point (`Cabitools.open`), and its manifest entry moves with it. Activity recreation after process death stays safe: the Activity builds `SdkContainer` itself, so there is no global holder that could be empty.

Alternative: a global dependency holder in presentation, set by the application module. After process death the Activity can be recreated before anything sets it.

**Infrastructure without the public config types.** `HeaderRules` keeps only its plain constructor (`redactDefaultHeaders`, `redactedHeaders`, `hiddenHeaders`); the mapping from `HttpLogsConfig` moves into the application module. `HttpLogsStore.retention: Retention` becomes `retentionMillis: Long` (default one day), and `Cabitools.httpInterceptor` sets it from `config.retention.millis`. `HttpLogsConfig`, `Retention` and their `internal` members stay in the application module, where `checkNoOpApi` compares them with the no-op.

### Visibility

All modules use `explicitApi()`. Declarations used by another module become `public`:

- domain: models, `ContentTypes`, repository interfaces, use cases.
- infrastructure: `AppInfoRepositoryImpl`, `DeviceInfoRepositoryImpl` (public no-argument constructor; the `DeviceBuildSource` one stays internal), `HeaderRules` (a data class of the header options) and `HttpLogs`, a small entry point with `interceptor(context, retentionMillis, headerRules, maxContentLength)` and `repository(context)`.
- presentation: `PresentationDependencies`, `CabitoolsNavHost`, `CabitoolsTheme`.

Everything else stays `internal`, including `HttpLogsStore`, `HttpLogsInterceptor`, `HttpLogSink`, the DAO and the Room entities: making the interceptor or the store public would have forced the sink, the DAO and the entities public too, so `HttpLogs` hands out only an `Interceptor` and a `HttpLogsRepository`. ViewModels, screens and formatting stay `internal` to presentation. Test-only accessors (`HttpLogsStore.saveAsync`) stay `internal`, with their tests in the same module.

### Build setup

- domain applies `org.jetbrains.kotlin.jvm`, pinned to the catalog `kotlin` version (AGP's 2.2.10), with Java 11, detekt and Kover. The first task builds it before anything moves, to confirm the plugin coexists with AGP's built-in Kotlin.
- infrastructure takes Room, KSP and the schema export (the directory follows the database class's new package); presentation takes Compose, Navigation 3, lifecycle and the theme; the application module keeps OkHttp `compileOnly` for the public `Interceptor` type, as does infrastructure.
- Kover is applied to all four modules; the application module adds `kover(project(...))` dependencies on the other three, so `:cabitools-sdk-application:koverVerify` checks the merged report at 90%. The merged report uses the application module's filters, so all exclusions (Activity, `di`, theme, Composables, Room `*_Impl`) are declared there, with their package names updated.
- `checkNoOpApi` compares the compiled classes of `:cabitools-sdk-application` and `:cabitools-sdk-application-no-op`; CI runs `:cabitools-sdk-application:koverVerify` instead of `:sdk:koverVerify`.
- The sample app depends on `:cabitools-sdk-application` (debug, QA) and `:cabitools-sdk-application-no-op` (release).

### Tests

Tests move with the code they cover:

| Module | Tests |
|---|---|
| domain | `ContentTypesTest`, use case tests |
| infrastructure | repository, `http` and `local` tests (Robolectric, MockWebServer); instrumented `AppInfoRepositoryImplTest` |
| presentation | screen, ViewModel, navigation, formatting and sharing tests, with fake `PresentationDependencies` |
| application | `HttpLogsConfigTest`, `CabitoolsOpenTest`, `CabitoolsHttpInterceptorTest`, a `SdkContainer` wiring test, a header rules mapping test; instrumented `CabitoolsTest` with `HostActivity` |
| application-no-op | `NoOpHttpInterceptorTest` (unchanged) |

`SettingsViewModelFactoryTest` becomes the `SdkContainer` wiring test. `HeaderRulesTest` and `HttpLogsInterceptorTest` build `HeaderRules` with its constructor.

## Risks / Trade-offs

- [The Kotlin JVM plugin clashes with AGP's built-in Kotlin] → Checked in the first task with an empty domain module. Fallback: make domain an Android library and keep the "no Android imports" rule with a detekt `ForbiddenImport` rule.
- [Host apps break until they switch modules] → Accepted and listed as breaking; imports don't change, only dependency lines. The interview project is updated separately.
- [Merged Kover coverage drops below 90% because a module has untested glue] → Each task runs `koverVerify`; untested glue gets tests, not exclusions.
- [More public declarations than before] → Public only inside the SDK's module graph; `implementation` keeps them off host compile classpaths, and `checkNoOpApi` still guards the host-facing API.

## Migration Plan

Host apps replace `:sdk` with `:cabitools-sdk-application` and `:sdk-no-op` with `:cabitools-sdk-application-no-op` in their dependencies or composite build substitutions. No code changes.
