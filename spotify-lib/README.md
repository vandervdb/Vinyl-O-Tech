# 📦 :spotify-lib

Spotify SDK integration: data, domain and network layers behind interfaces consumed by
`:app`.

---

## Overview

- Language: Kotlin (JDK 17), Android library
- Network: Ktor + kotlinx-serialization
- DI: Hilt, **one module per concern** — 20 files under `di/` (`AuthModule`,
  `NetworkModule`, `PlayerModule`, `RepositoryModule`, …). Match that granularity: do
  not add a second concern to an existing module file, create a new one
- Project deps: `:core:domain`, `:core:logger`, `:core:dto`, `:core:ui`
- Consumed by: `:app`

---

## Local AARs

`libs/spotify-*.aar` are the Spotify SDK binaries, pulled in as
`compileOnly(files("libs/..."))`. They are a **known, accepted exception** to the
version-catalog rule: a binary sitting in the tree cannot go through
`libs.versions.toml`.

Note the gate does not whitelist them — `checkVersionHardcodedUsages` looks for
hardcoded `"group:artifact:version"` coordinates, and a `files(...)` path simply is not
one. They pass by shape, not by exemption. Bumping one means renaming the file, so the
version stays visible in the diff.

---

## Session state

`SpotifySessionManager` exposes `sessionState: StateFlow<SessionState>` and is bound
`@Singleton` in `SingletonComponent`, so every consumer — ViewModel or `@EntryPoint` —
observes the same instance. The flow is the app's single source of truth for
authorisation and remote connection:

```
Idle → Authorizing → ConnectingRemote → Ready
                  ↘ Failed(Throwable)
```

`requestAuthorization()` sets `Authorizing` and `launchAuthorizationFlow()` immediately
hands control to the Spotify authorisation activity — so most of that phase happens
while the app is in the background. Anything you want the user to *see* has to key off
`ConnectingRemote` or off the return.

---

## Build & test

```bash
./gradlew :spotify-lib:assembleDebug
./gradlew :spotify-lib:testDebugUnitTest
./gradlew :spotify-lib:connectedAndroidTest    # runner org.vander.spotifyclient.HiltTestRunner
```

---

## Known debt

- `src/test/kotlin` uses the package root `com.vander.spotifyclient`, while `main` and
  `androidTest` use `org.vander.spotifyclient`. New tests go under `org.vander` — do not
  propagate the mismatch
- `data/local/DataStoreManager.kt` has 3 `TODO("Not yet implemented")`
- `FakePlayerStateRepository`, `FakeLibraryRepository` and `FakeSpotifySessionManager`
  have `TODO()` bodies on methods no current test exercises — stubs, not broken tests
