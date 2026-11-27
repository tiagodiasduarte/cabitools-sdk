# Tasks

## 1. Rename the code

- [x] 1.1 Move the `:sdk`, `:sdk-no-op` and `:app` sources (main, test, androidTest) to the `pt/tiagodiasduarte/cabitools/sdk` and `pt/tiagodiasduarte/cabitools/sample` directories with `git mv`, and rename files named after old types. Verify that `git status` shows renames, not deletions and additions
- [x] 1.2 Replace the old names in code, build scripts, manifests and resources per design.md (packages, namespaces, `AppSettings` identifiers, `appsettings_` prefix, "App Settings" strings, root project and sample app names), and move the Room schema export directory. Verify that `./gradlew assembleDebug checkNoOpApi` succeeds and the API dump shows `pt/tiagodiasduarte/cabitools/sdk/Cabitools`
- [x] 1.3 Delete the old `appsettings_http_logs.db` when the database is created. Verify with a Robolectric test that an existing old database file is removed and the new one is used

## 2. Docs

- [x] 2.1 Update `sdk/README.md` (name, origin of the name, snippets, database name) and `CLAUDE.md` (module descriptions, namespaces, prefix, commands). Verify that the README snippets match the sample app's code

## 3. Checks and repository

- [x] 3.1 Search the repository for `appsettings`, `AppSettings`, "App Settings" and `app-settings` outside `openspec/changes/archive`, and verify that nothing is found except the old database name in the cleanup code
- [x] 3.2 Run `./gradlew assembleDebug test :sdk:koverVerify lint detekt checkNoOpApi` and the instrumented tests on an emulator, and verify that all pass
- [x] 3.3 Rename the GitHub repository to `cabitools-sdk` and point the local `origin` remote at the new URL. Verify with `git remote -v` and `gh repo view`

## Workflow follow-up

- Open the PR from `refactor/rename-to-cabitools` into `develop`, and archive the change as its last commit.
- Update host apps (android-interview-challenge) to the new names separately.
