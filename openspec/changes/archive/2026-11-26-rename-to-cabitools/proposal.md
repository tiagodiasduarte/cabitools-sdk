# Proposal

## Why

"App Settings" no longer describes the SDK: it is now a set of debug and QA tools (app and device info, HTTP logs, more to come), not a settings screen. The SDK gets its own name, **Cabitools** (after *cabidela*), before more host apps and SDKs integrate it and the old name spreads further.

## What Changes

- **BREAKING** Rename the public entry point `AppSettings` to `Cabitools` (`Cabitools.open(context)`, `Cabitools.httpInterceptor(context) { ... }`) in both `:sdk` and `:sdk-no-op`. `HttpLogsConfig`, `Retention` and `HttpLogsDsl` keep their names.
- **BREAKING** Move the packages from `pt.tiagodiasduarte.appsettings.sdk` to `pt.tiagodiasduarte.cabitools.sdk` (namespaces `pt.tiagodiasduarte.cabitools.sdk` and `pt.tiagodiasduarte.cabitools.sdk.noop`).
- Rename the internal types that carry the old name (`AppSettingsActivity`, `AppSettingsNavHost`, `AppSettingsRoute`, `AppSettingsTheme`) to their `Cabitools` equivalents.
- Change the resource prefix from `appsettings_` to `cabitools_`, and the screen title from "App Settings" to "Cabitools".
- Rename the HTTP logs database to `cabitools_http_logs.db`, and delete the old `appsettings_http_logs.db` file once, so it does not stay behind on test devices.
- Rename the Gradle root project to `cabitools-sdk`, the sample app to "Cabitools Sample" (namespace and application id `pt.tiagodiasduarte.cabitools.sample`), and update the README and `CLAUDE.md`.
- Rename the GitHub repository from `app-settings-sdk` to `cabitools-sdk`.

Out of scope: renaming the local checkout folder, updating host apps (for example android-interview-challenge, whose imports and composite build substitution must move to the new names separately), and renaming archived OpenSpec changes, which keep their history.

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
<!-- None: no spec names the SDK, its entry point or its packages, and behavior does not change. The change sets skip_specs: true. -->

## Impact

- **Host apps** must update imports (`pt.tiagodiasduarte.cabitools.sdk.Cabitools`), calls (`Cabitools.open`, `Cabitools.httpInterceptor`), and dependency coordinates or composite build substitutions. Their logged HTTP calls from before the update are discarded with the old database.
- **Code:** every source file in `:sdk`, `:sdk-no-op` and `:app` moves to the new package; resources, keep-free Kover filters and the Room schema export path follow the new names.
- **Repository:** GitHub redirects the old URL after the rename; the local `origin` remote is updated to the new URL.
