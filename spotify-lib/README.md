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

## Two channels, one session

Spotify is reachable in two unrelated ways, and this module speaks both. Almost every
design decision below follows from that split.

| | **App Remote** (local AAR) | **Web API** (HTTP) |
|---|---|---|
| Transport | IPC to the Spotify app installed on the device | Ktor + OkHttp over HTTPS |
| Provides | current track, position, play / pause / skip / seek / shuffle / repeat | queue, saved tracks, playlists, profile |
| Shape | **push** — a callback fires on every player change | **pull** — one `suspend` call, one `Result` |
| Credential | the connection itself is authorised, no token passed | `Authorization: Bearer <token>` |
| Fails when | the Spotify app is missing, logged out, or refuses | the token is missing or expired |
| Entry point | `AppRemoteProvider` → `PlayerClient` | `@Named` `HttpClient` → `Remote*DataSource` → repository |

Neither channel knows the other exists. Reconciling them is the job of `PlayerUseCase`,
and that is the single most subtle class of the module — see *Merging the two channels*.

---

## Layers

```
                         :app   ·   non-Hilt host (bridge/)
                                      │
  ┌───────────────────────────────────┴─────────────────────────────────────┐
  │ use case      PlayerUseCase · PlaylistUseCase · SpotifyRemoteUseCase     │
  │               merge + orchestration, exposed as StateFlow               │
  ├─────────────────────────────────────────────────────────────────────────┤
  │ session       SpotifySessionManager — authorization ▸ remote, one flow   │
  ├─────────────────────────────────────────────────────────────────────────┤
  │ repository    PlayerStateRepository · LibraryRepository ·               │
  │               SpotifyQueueRepository · SpotifyPlaylistRepository ·      │
  │               UserRepository · IAuthRepository                          │
  ├──────────────────────────────────┬──────────────────────────────────────┤
  │ data source   PlayerClient       │ Remote{Queue,Library,Playlist,User,  │
  │               (App Remote)       │ Auth}DataSource   (Web API)          │
  ├──────────────────────────────────┼──────────────────────────────────────┤
  │ transport     RemoteConnector    │ HttpClient + AuthHeaderPlugin        │
  │               ▸ SpotifyAppRemote │ ▸ api.spotify.com / accounts.…       │
  └──────────────────────────────────┴──────────────────────────────────────┘
                                      │
                        DataStoreManager  ·  spotify_prefs (access token)
```

Every layer depends on the **interface** of the one below (`domain/`), never on its
implementation — that is what lets a unit test replace the App Remote or the network with
a fake. `data/` holds the implementations, `domain/` the contracts, and the Hilt modules
under `di/` are the only place the two meet.

Interfaces meant to cross the module boundary (`IAuthRepository`, `ITokenProvider`,
`PlayerStateRepository`, `SessionState`, `PlayerStateData`) live in `:core:domain`, not
here: `:app` and `:fake` both implement or consume them without depending on this module.

---

## The authorization flow

The flow is split across three calls on `SpotifySessionManager` because **the app leaves
the foreground in the middle of it**: the Spotify login screen is another Activity, so a
single `suspend fun authorize()` could not span it.

```
AppRoot (Compose)          SpotifySessionManager        Spotify app / accounts API
     │
     │ requestAuthorization(launcher)
     ├──────────────────────────►│  keeps the launcher · state = Authorizing
     │
     │ launchAuthorizationFlow(activity)
     ├──────────────────────────►│  authClient.authorize()
     │                           ├───────────────────────────►  LoginActivity
     │                           │                               (app backgrounded)
     │ ◄───── ActivityResult ────────────────────────────────────────┘
     │
     │ handleAuthResult(ctx, result, scope)
     ├──────────────────────────►│  code ─► AuthRemoteDataSource.fetchAccessToken()
     │                           │       ─► DataStoreManager.saveAccessToken()
     │                           │  state = ConnectingRemote
     │                           │  remoteProvider.connect(ctx)
     │                           │       ─► SpotifyAppRemote.connect()
     │                           │  state = Ready  |  Failed
```

Three consequences worth knowing before touching this:

- **The launcher cannot live in a ViewModel.** `registerForActivityResult` is bound to an
  Activity lifecycle; the UI owns it and hands it over. That is also why the manager's
  signatures carry `Activity` / `ActivityResult` — see the KDoc on the interface.
- **The caller supplies the `CoroutineScope`.** The manager is `@Singleton` and outlives
  every screen, so it must not own the scope the token exchange runs in. `AppRoot` passes
  `lifecycleOwner.lifecycleScope`; cancelling it aborts the session mid-flight.
- **The code-for-token exchange is a Web API call**, made with its own client
  (`@Named("AuthHttpClient")`, Basic auth, no bearer plugin) — the bearer plugin would
  send a token that does not exist yet.

---

## Session state

`SpotifySessionManager` exposes `sessionState: StateFlow<SessionState>` and is bound
`@Singleton` in `SingletonComponent`, so every consumer — ViewModel or `@EntryPoint` —
observes the same instance. The flow is the app's single source of truth for
authorisation and remote connection:

```
Idle → Authorizing → ConnectingRemote → Ready
                  ↘ Failed(SessionError)
```

`requestAuthorization()` sets `Authorizing` and `launchAuthorizationFlow()` immediately
hands control to the Spotify authorisation activity — so most of that phase happens
while the app is in the background. Anything you want the user to *see* has to key off
`ConnectingRemote` or off the return.

`shutDown()` disconnects the App Remote and returns to `Idle` **keeping the token**, so a
later start-up reconnects without a login screen. `signout()` is the one that clears it.

`AppRemoteProvider.remoteState` is a second, narrower flow (`NotConnected` / `Connecting`
/ `Connected` / `Failed`) covering only the App Remote link. `SessionState` is what the UI
collects; `remoteState` is the detail underneath it.

---

## Reading and writing the player

`PlayerClient` is the whole App Remote surface. Two properties of the SDK shape it:

- **Commands never return the resulting state.** `pause()` returns as soon as the command
  is accepted; the new state arrives later through the subscription. So an optimistic UI
  update has to be written by hand — and `PlayerUseCase.togglePlayPause()` currently
  cancels its own by toggling twice.
- **Subscriptions must be cancelled.** `subscribeToPlayerState` /
  `unsubscribeFromPlayerState` are paired; the SDK subscription leaks otherwise.

`DefaultPlayerStateRepository` republishes those pushes as a `StateFlow`, and infers one
extra signal from them: a push carrying a state **equal** to the previous one is read as
"the track was saved/unsaved from another device" and published as a one-shot on
`savedRemotelyChangedState`. That is an assumption about the SDK's behaviour, not a
documented guarantee — any other cause of a duplicate push is a false positive.

---

## Merging the two channels

`PlayerUseCase` is what the player screen actually talks to. It runs three collectors
from `startUp()`, in a scope that must outlive the screen:

| Collector | Reads | Writes |
|---|---|---|
| `updateSpotifyPlayerStateAndUIQueueState` | `combine(currentUserQueue, playerStateData)` | `domainPlayerState`, `uIQueueState` |
| `collectSessionState` | `sessionState` | triggers the queue fetch + `startListening()` on `Ready` |
| `observeSavedRemotelyChangedState` | `savedRemotelyChangedState` | refreshes the saved flag |

The hard part is that the App Remote *pushes* a state on every tick while the queue is a
*snapshot* that has to be re-fetched. `hasReceivedUpdatedQueue` is the guard: the queue is
only published once its `currentlyPlaying` matches the track the remote reports, and it is
re-fetched as soon as the playing track falls outside the queue that is displayed.

Rough edges in the current implementation — documented in the class KDoc, listed here so
they are not mistaken for intent:

- `togglePlayPause()` flips the pause flag, sends the command, then flips it back
- `toggleSaveTrackState()` only updates local state; it never calls `libraryRepository`
- `playerStateRepository` and `playerRepository` are two constructor parameters bound to
  the same instance
- the logger is built with `KermitLoggerImpl` instead of being injected

---

## The Hilt graph

20 modules under `di/`, one per concern, all `@InstallIn(SingletonComponent::class)`.
Two patterns, applied consistently:

- **`@Binds` on an `abstract class`** for every interface → implementation link Hilt can
  already build (`AuthModule`, `RepositoryModule`, `UseCaseModule`, …). No factory body is
  generated, so it is cheaper than `@Provides`
- **`@Provides` on an `object`** only for what Hilt cannot construct: the Ktor clients,
  the DataStore, the SDK connector (`NetworkModule`, `KtorClientConfigModule`,
  `RemoteModule`)

The Ktor clients are the one place worth reading in full. `NetworkModule.provideKtorClient`
is a single builder parameterised by a `KtorClientConfig` data class, and the named
qualifiers pick which one you get:

| Qualifier | Base URL | Bearer plugin | Used by |
|---|---|---|---|
| `AuthHttpClient` | `accounts.spotify.com/api/` | no (Basic header) | `AuthRemoteDataSource` |
| `auth_api_v1_client` | `api.spotify.com/v1/` | yes | the four `Remote*DataSource` |
| `public_api_v1_client` | `api.spotify.com/v1/` | no | **nothing — declared, never injected** |

`AuthHeaderPlugin` reads the token from `ITokenProvider` *inside* the request interceptor,
once per call, so a token written to DataStore after the client was built is picked up
without rebuilding anything. `DataStoreTokenProvider` is the read-only adapter that gives
the network layer the token without giving it the ability to clear the session.

---

## What `:app` actually touches

The public surface is deliberately small — `utils/Constants.kt` is `internal`, so a
consumer configures the library through `AuthConfigK`, not by reading its endpoints:

| Type | Where it is used in `:app` |
|---|---|
| `SpotifySessionManager` | injected into `ConnectionViewModelImpl` / `PlayerViewModelImpl`; reached from `AppRoot` through `SpotifySessionEntryPoint` |
| `PlayerUseCase`, `PlaylistUseCase` | `PlayerViewModelImpl`, `PlayListViewModelImpl` |
| `LibraryRepository`, `UserRepository` | ViewModels, by constructor injection |
| `SpotifyAuthorizationActivity` | `MainActivity` extends it |

`SpotifyAuthorizationActivity` carries no logic: the point is the manifest declaration in
this module, which registers the `VIEW`/`BROWSABLE` intent filter for the redirect URI.
Scheme and host come from the `redirectSchemeName` / `redirectHostName` manifest
placeholders, set in **both** `spotify-lib/build.gradle.kts` and `app/build.gradle.kts`,
and must keep matching `REDIRECT_URI` — three places for one value.

`rememberSpotifySessionManager()` in `:app` reaches the singleton through an
`@EntryPoint` rather than by injection, because a `@Composable` is not a Hilt-constructed
object.

---

## The bridge — for a host that is not this app

`bridge/` is a facade for a consumer that cannot depend on Hilt, typically a React Native
TurboModule. `SpotifyBridgeApi` exposes only its own DTOs (`PlayerStateDto`, `AuthConfigK`,
`AuthResult`), never the SDK or domain types, plus `getXxx()` accessors for a host that
cannot collect a Kotlin flow.

Two pieces exist only here:

- `obtainBridgeFromHilt(context)` + `SpotifyEntryPoint` — the escape hatch into the graph
  for an object Hilt did not build
- `ActivityResultFactory` — registers an `ActivityResultLauncher` *after* the Activity has
  started, by adding a headless Fragment. `registerForActivityResult` normally has to be
  called before `STARTED`, which a library called from an arbitrary host cannot guarantee

The bridge owns its own `CoroutineScope` (no host lifecycle to hang off), so `onDestroy()`
must be called or the scope leaks. `:app` does not use any of this.

---

## Local AARs

`libs/spotify-*.aar` are the Spotify SDK binaries, pulled in as
`compileOnly(files("libs/..."))`. They are a **known, accepted exception** to the
version-catalog rule: a binary sitting in the tree cannot go through
`libs.versions.toml`.

`compileOnly` rather than `implementation` because AGP refuses to bundle a local AAR into
a published AAR — the consuming app is what puts them on the runtime classpath.

Note the gate does not whitelist them — `checkVersionHardcodedUsages` looks for
hardcoded `"group:artifact:version"` coordinates, and a `files(...)` path simply is not
one. They pass by shape, not by exemption. Bumping one means renaming the file, so the
version stays visible in the diff.

---

## Testing seams

The module is unit-testable without a device because three static SDK entry points are
wrapped behind interfaces:

| Seam | Hides | Fake in tests |
|---|---|---|
| `RemoteConnector` | `SpotifyAppRemote.connect` (static, unmockable) | `FakeConnector` |
| `PlayerClient` | `PlayerApi` callbacks | `FakeSpotifyPlayerClient` |
| `SpotifyAuthClient.parseAuthResponse` | `AuthorizationClient.getResponse` (static) | overridden in a subclass |

`AppRemoteProvider.getRemoteHandle()` returns `Any?` for the same reason: a test can hand
back a plain object where production hands back a `SpotifyAppRemote`.

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
- **No token refresh.** `TokenResponseDto` carries a refresh token, but
  `DataStoreManager`'s three refresh-token methods are `TODO("Not yet implemented")` and
  nothing calls them. When the access token expires the Web API calls start failing and
  the only way out is a new authorization
- `domain/player/` holds three files — `IDataStoreManager`, `IAuthRemoteDatasource`,
  `ISpotifyAuthClient` — duplicating the ones in `domain/auth/`. Nothing imports the
  `domain.player` copies
- `KtorClientConfigModule` declares `public_api_v1` / `public_api_v1_client`; no data
  source injects them
- `model/api/NowPlaying.kt` is referenced nowhere
- `DataStoreManager` declares the `Context.dataStore` delegate twice on the same
  `spotify_prefs` file (file level + class level); only the class-level one is reachable
- **Secrets in logcat on debug builds**: `AuthRemoteDataSource` logs the Base64
  credentials and the raw token response, `DataStoreManager.saveAccessToken` logs the
  token, `parseSpotifyResult` logs raw bodies including the `me` endpoints
- `PlayerClient.playerConnectionState` and `lastState` are written but never collected —
  connection state is actually observed on `AppRemoteProvider.remoteState`
- `SpotifyRemoteConnector.connect` ignores its `redirectUrl` parameter and uses the
  `REDIRECT_URI` constant instead
- `FakePlayerStateRepository`, `FakeLibraryRepository` and `FakeSpotifySessionManager`
  have `TODO()` bodies on methods no current test exercises — stubs, not broken tests
