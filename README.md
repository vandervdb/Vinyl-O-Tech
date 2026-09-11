# 🎧 Vinyl O’Tech

<p align="center">
  <strong>Native Android client for the Spotify Web and App Remote APIs</strong><br />
  <em>Your Spotify library, presented as a record collection.</em>
</p>

<p align="center">
<img alt="Android" src="https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white&style=for-the-badge" />
<img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.4.0-7F52FF?logo=kotlin&logoColor=white&style=for-the-badge" />
<img alt="Compose" src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white&style=for-the-badge" />
<img alt="Hilt" src="https://img.shields.io/badge/DI-Hilt%202.58-blue?style=for-the-badge" />
<img alt="Gradle" src="https://img.shields.io/badge/Gradle-8.14.5-02303A?logo=gradle&logoColor=white&style=for-the-badge" />
<img alt="Architecture" src="https://img.shields.io/badge/Architecture-MVVM%20%7C%20Multi--Module-6E7FF3?style=for-the-badge" />
<img alt="License" src="https://img.shields.io/badge/License-MIT-brightgreen?style=for-the-badge" />
</p>

---

## Overview

### ❓ Why this project?

Saved albums, playlists, liked songs, followed artists — Spotify already knows what you
own. Vinyl O’Tech presents it the way a record collection actually feels, and uses that
as an excuse to answer engineering questions properly:

- How far can a Compose design system go without a single XML layout?
- Where does a ViewModel’s scope actually end, and what does `NavBackStackEntry` change?
- What belongs to the screen and what belongs to the window — navigation chrome,
  overlays, a snackbar that must outlive the screen that raised it?
- How do you keep the dependency graph a DAG when eight modules are in play?
- How do you make quality gates *actually* gate, instead of printing warnings nobody
  reads?

This repository is a deliberate playground. The goal is not only that the code runs, but
that it stays readable: patterns justified, trade-offs written down, and technical debt
**documented rather than hidden** — see [Known debt](#known-debt).

> Extracted from the `spotify-monorepo`, whose React Native tree weighed 7.7 GB. Kotlin
> packages are unchanged since the extraction; only the module layout was flattened.

---

## Tech stack

| | |
|---|---|
| Language | Kotlin 2.4.0, JDK 17 |
| UI | Jetpack Compose + Material 3 — no XML layouts, no View system |
| DI | Hilt 2.58 — 20 focused modules in `spotify-lib` alone |
| Build | Gradle 8.14.5 (wrapper) · AGP 8.13.2 · compileSdk 35 · minSdk 26 |
| Compose | compiled by `org.jetbrains.kotlin.plugin.compose`, **not** `composeOptions` — BOM 2025.12.00 |
| Persistence | DataStore, Tink (crypto, work in progress) |
| Network | Ktor + kotlinx-serialization |
| Logging | in-house `Logger` interface backed by Kermit |
| Tests | JUnit4 · MockK · Turbine · `kotlinx-coroutines-test` |
| Hooks | Lefthook |

---

## Project structure

```
vinyl-otech/
├─ app/                          # Compose application, navigation, Hilt entry points
│  └─ src/main/kotlin/org/vander/android/sample/
│     ├─ AppRoot.kt              # root Box: content, grain overlay, snackbar host
│     ├─ navigation/             # AppNavHost · AppBottomBar · NavItem
│     ├─ designsystem/           # Color · Type · Theme · VotDimens · Surfaces
│     │  ├─ modifier/            # visual Modifier extensions
│     │  └─ component/           # domain-free primitives (VinylDisc, VinylLoader…)
│     ├─ component/              # shared across features, domain-aware
│     ├─ feature/<name>/         # vertical slice: screen + nav graph + ViewModel
│     ├─ util/                   # composable helpers
│     └─ di/                     # Hilt modules and entry points
├─ spotify-lib/                  # Spotify SDK integration — data / domain / network
│  └─ libs/                      # local Spotify AARs (catalog exception)
├─ core/
│  ├─ domain/                    # pure Kotlin interfaces & models, zero Android
│  ├─ dto/                       # kotlinx-serialization DTOs
│  ├─ logger/                    # Logger interface + Kermit impl + Hilt module
│  ├─ ui/                        # cross-module ViewModel contracts & UI state
│  └─ security/                  # Tink + DataStore, api/ + impl/ split — orphan, WIP
├─ fake/                         # fake ViewModels for @Preview
├─ config/detekt/detekt.yml
├─ docs/architecture/
├─ .claude/rules/                # repo conventions, enforced
└─ lefthook.yml
```

The dependency graph is a DAG rooted at the three pure leaves (`core:domain`,
`core:dto`, `core:logger`) → `core:ui` / `fake` → `spotify-lib` → `app`. No edge ever
points the other way.

`app` is organised **by feature, not by layer**. The old `ui/screen` + `ui/components`
split was abandoned: with ten screens coming, adding one touched four sibling
directories. A feature directory is a vertical slice — screen, nav graph, ViewModel, own
components. The design system lives apart, in `designsystem/`.

---

## Requirements

- JDK 17
- Android SDK, `ANDROID_HOME` set
- A Spotify developer application (client ID + secret)
- The Spotify app installed on the device, for App Remote playback

---

## Setup & run

Credentials are read from `local.properties` at the repo root. The file is
git-ignored and **must never be committed**:

```properties
CLIENT_ID=xxxx
CLIENT_SECRET=yyyy
```

`settings.gradle.kts` reads them and exposes them through `BuildConfig`; without them
the Gradle configuration fails loudly rather than building a broken APK.

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
./gradlew clean

lefthook install        # once, to enable the git hooks
```

---

## Quality gates

These are the arbiters — not the IDE’s inspections.

```bash
./gradlew test                          # unit tests, every module
./gradlew lint
./gradlew assembleDebug

./gradlew checkCatalogConsistency       # every declared version must be used
./gradlew checkVersionHardcodedUsages   # every dependency must go through libs.*
./gradlew checkColorPalette             # Color.kt ↔ res/values/colors.xml
```

The three verification tasks are written in the root `build.gradle.kts`.
`checkColorPalette` pairs each Compose token with its XML counterpart through the
trailing `// vot_*` comment the token carries, and fails on any value mismatch or on a
comment pointing at a missing `<color>`. XML-only tokens are reported, not failed.

### Git hooks (Lefthook)

| Stage | Runs |
|---|---|
| pre-commit | Spotless, `ktlintFormat` |
| pre-push | `lint`, `test`, the three gates above, `assembleDebug` |

Two things worth knowing, because they contradict what is easy to assume:

- **Spotless does fail the commit.** `ktlintCheck` carries `ignoreFailures`, but
  `scripts/spotless-pre-commit.sh` runs with `set -e`, so a rule that cannot be
  auto-fixed — `standard:filename`, `standard:property-naming`,
  `standard:max-line-length`, `standard:no-empty-file` — aborts the commit.
- **It runs project-wide, not on staged files only.** A single empty `.kt` left anywhere
  in the tree blocks *every* commit, whatever the index contains.

**detekt is not in the build.** No Gradle plugin, no hook — only the IntelliJ plugin runs
it. `config/detekt/detekt.yml` is a *partial* config (it exempts `@Composable` from
`FunctionNaming`, since a composable is named like a type), so the plugin needs
*Build upon the default configuration* checked. Without it, every unlisted rule silently
stops running.

---

## Documentation

| File | What it holds |
|---|---|
| `CLAUDE.md`, `.claude/rules/` | repo conventions — architecture, Kotlin, UI, testing |
| `docs/architecture/navigation-and-screen-chrome.md` | two-register navigation (push / modal), `ScreenChrome`, ViewModel scoping, design measurements |
| `app/README.md`, `spotify-lib/README.md`, `fake/README.md` | per-module notes |

---

## Known debt

Assumed and documented, not hidden. Do not fix it as a side effect of another task.

| Where | What |
|---|---|
| `core:security` | declared by no module — in-progress infrastructure, not dead code. `TinkCryptoEngine` has 2 `TODO()` |
| `spotify-lib/src/test` | package root `com.vander.spotifyclient`, while `main` and `androidTest` use `org.vander.spotifyclient` |
| `DataStoreManager.kt` | 3 `TODO("Not yet implemented")` |
| `SpotifyScreen.kt` | `SessionState.IsPaused -> TODO()` in a `when` — a real gap, not a test stub |
| `AppRoot.kt` | builds its `Logger` by hand although `LoggerModule` provides one as `@Singleton` |
| `AppBottomBar.kt` | keeps a `remember { mutableStateOf(0) }` duplicating `NavController` state |
| `fake/FakePlayerViewModel.kt` | several actions do not update state |
| UI strings | many still hardcoded in Composables rather than in `strings.xml` |
| `values-night/themes.xml` | dead since the theme went dark-only |

---

## License

[MIT](LICENSE) — © 2026 Arnaud Vanderbecq.

The licence covers this repository's own source. It does not extend to the Spotify SDK
binaries under `spotify-lib/libs/`, which carry Spotify's own terms. Spotify trademarks
belong to Spotify AB; this repository is neither affiliated with nor endorsed by
Spotify.
