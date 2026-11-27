# Design

## Context

See proposal.md - Why. The old name appears in package paths, the public `AppSettings` object, internal type names, the `appsettings_` resource prefix, user-visible strings, the HTTP logs database file name, the Room schema export directory, Kover filters, the sample app and the docs. No spec mentions it.

## Goals / Non-Goals

**Goals:**
- One mechanical rename with no behavior change beyond the new names.
- `:sdk` and `:sdk-no-op` keep identical public APIs (`checkNoOpApi`).
- No leftover old database file on devices that ran an earlier SDK.

**Non-Goals:**
- A deprecated `AppSettings` alias for a transition period. Only my own apps use the SDK, so a clean break is simpler than keeping two names.
- Migrating logs from the old database: they expire within a week anyway.

## Decisions

### Names

| Old | New |
|---|---|
| `AppSettings` (public) | `Cabitools` |
| `pt.tiagodiasduarte.appsettings.sdk` | `pt.tiagodiasduarte.cabitools.sdk` (`.noop` for the no-op namespace) |
| `AppSettingsActivity`, `AppSettingsNavHost`, `AppSettingsRoute`, `AppSettingsTheme` | `CabitoolsActivity`, `CabitoolsNavHost`, `CabitoolsRoute`, `CabitoolsTheme` |
| `appsettings_` resources, `appsettings_Theme` | `cabitools_`, `cabitools_Theme` |
| "App Settings" screen title | "Cabitools" |
| `appsettings_http_logs.db` | `cabitools_http_logs.db` |
| `rootProject.name = "App Settings Sdk"` | `cabitools-sdk` |
| Sample `pt.tiagodiasduarte.appsettings`, "App Settings Sdk" | `pt.tiagodiasduarte.cabitools.sample`, "Cabitools Sample" |

Test classes named after old types follow them (for example `AppSettingsOpenTest` becomes `CabitoolsOpenTest`).

### Rename mechanics

Files move with `git mv` so history follows them. Text replacements run in a fixed order, most specific first: the SDK package, then the sample app package, then `AppSettings` identifiers, then the lowercase `appsettings` prefix and strings. A final search for `appsettings`, `AppSettings` and "App Settings" outside `openspec/changes/archive` must return nothing.

### Old database cleanup

`HttpLogsDatabase.create` calls `context.deleteDatabase("appsettings_http_logs.db")` before opening the new database. `deleteDatabase` returns false when the file does not exist, so it is safe to run on every start, and it is one line, cheaper than tracking whether it already ran.

Alternative: keep the old file name. This leaves "appsettings" in a place host app developers see (device file explorer) and splits the naming for no gain.

### Room schema export

The exported schema lives in a directory named after the database class's fully qualified name, so it moves from `sdk/schemas/pt.tiagodiasduarte.appsettings.sdk.data.local.HttpLogsDatabase/` to the `cabitools` path. The schema itself (version 1, two tables) does not change.

### GitHub repository

`gh repo rename cabitools-sdk` renames the repository; GitHub keeps redirects from the old URL. The local `origin` remote is set to the new URL right after.

## Risks / Trade-offs

- [Host apps break until updated] → Accepted: a clean break, listed in the proposal. The interview project is updated separately.
- [A missed occurrence of the old name] → The final repository-wide search and a full build, test, lint, detekt and `checkNoOpApi` run catch them.
- [Open branches based on old paths, for example `chore/git-conventions`] → They conflict on rebase only where they touch renamed files; that branch only touches docs and `.gitignore`.
