# Architecture Rules — Android/Kotlin tree

**Rule statuses**:
- **[Enforced]** — applies to all code you write or touch, right now.
- **[Target — new code]** — mandatory for new screens/features; existing code does NOT follow it yet. Never rewrite existing code to comply unless that refactor IS the task.

This covers `spotify-lib/`, `app/`, `core/*`. For the TS/RN tree, see `.claude/rules/typescript.md` — the two trees do not share code or build together.

---

## Module map [Enforced — verified from each module's `build.gradle.kts`, not assumed]

| Module | Role | Depends on (project modules) |
|---|---|---|
| `core:domain` | Pure Kotlin/JVM interfaces & models (`ITokenProvider`, `IAuthRepository`, `SessionState`, ...). Zero Android/Compose/Hilt dependency | *(none — leaf)* |
| `core:dto` | Pure Kotlin/JVM DTOs, kotlinx-serialization | *(none — leaf)* |
| `core:logger` | Kermit-backed `Logger` interface + Hilt module | *(none)* |
| `core:security` | Tink-based crypto + DataStore keyset storage, `api/`+`impl/` split | *(none)* — **orphan**, see below |
| `core:ui` | Shared Compose design system + cross-module contracts | `core-domain` (`api`, so domain types leak through its public surface) |
| `fake` | Fake ViewModels for previews/dev builds (`FakePlayerViewModel`, `FakePlaylistViewModel`, `FakeUserViewModel`) | `core-domain`, `core-ui` |
| `spotify-lib` | Real Spotify SDK integration: data/domain/network layers, 18 focused Hilt modules under `di/` | `core-domain`, `core-logger`, `core-dto`, `core-ui` |
| `app` | Compose application, navigation, Hilt entry points | `spotify-lib`, `core-domain`, `core-logger`, `core-ui`, `fake` |

**Dependency direction**: a DAG rooted at the three leaves (`core-domain`, `core-dto`, `core-logger`) → `core-ui`/`fake` → `spotify-lib` → `app`. Do not add an edge going the other way (e.g. `core-domain` depending on `core-ui`, or `spotify-lib` depending on `app`) without flagging it first.

**`core-security` is currently disconnected from this graph** — it's registered in `settings.gradle.kts` and actively being refactored (Hilt wiring mid-migration from `SecurityModule.kt` to `di/SecurityDataStoreProvider.kt`), but no other module lists it as a dependency yet. Treat it as in-progress infrastructure, not dead code: don't wire it into `spotify-lib`/`app` as a side-effect of an unrelated task, and don't assume its current shape is final — check its actual files before extending it.

---

## Internal `api/` + `impl/` split — `core-security` only [Enforced, this module]

`core-security` separates public contracts from implementation, unlike every other `core/*` module:

- `api/` — interfaces: `CryptoEngine`, `KeysetRepository`, `SecureTokenStorage`, `KeyRotationManager`
- `impl/tink/` — Tink-backed implementations (`TinkCryptoEngine`, `TinkKeysetHandleProvider`)
- `impl/storage/` — DataStore-backed implementations (`DataStoreKeysetRepository`)

New security primitives follow this split (interface in `api/`, implementation in `impl/<tech>/`). Do not introduce this split in other modules without discussing it — it's specific to `core-security`'s multi-backend nature (Tink today, could have another crypto backend later), not a repo-wide pattern.

---

## Legacy code & known tech debt [Enforced]

Known debt — assume it, don't be surprised by it, don't silently fix it:

- **Package root mismatch**: `spotify-lib/src/test/kotlin` uses `com.vander.spotifyclient` while main/`androidTest` use `org.vander.spotifyclient` (see `kotlin.md`)
- **Unhandled state in production code**: `app/.../ui/screen/SpotifyScreen.kt:121` has `SessionState.IsPaused -> TODO()` in a `when` — this is a real gap, not a test stub. If your task touches this `when`, flag it; don't silently implement a guess at the intended behavior
- **Unimplemented DataStore methods**: `spotify-lib/.../data/local/DataStoreManager.kt` has 3 `TODO("Not yet implemented")` methods
- **No-op fake action handlers**: `fake/.../FakePlayerViewModel.kt` has several `// TODO` comments where actions don't update state — expected for a preview fake, but don't assume they're wired up if a task depends on fake state actually changing
- **`core-security` crypto engine incomplete**: `TinkCryptoEngine.kt` has 2 unimplemented `TODO()` methods — consistent with the module being orphaned/WIP above
- UI strings are largely hardcoded inline in Composables rather than via `strings.xml` — do not silently migrate as a side-effect of an unrelated task; report it instead

**How to behave in legacy/debt areas:**
1. **Boy-scout, scoped**: when your task touches debt-laden code, improve only the part you modify. Never refactor a whole file "while you're there"
2. **Don't widen scope**: a refactor beyond the task at hand must be proposed, not done. Report what you found; let the user decide
3. **Never make debt worse**: no new hardcoded module wiring outside `settings.gradle.kts` conventions, no new cross-module dependency beyond the documented DAG, no new orphan module

---

## MVVM pattern [Target — new code, matches most existing code]

| Role | Rules |
|---|---|
| **Model / domain** | Plain Kotlin — data classes, use cases, repository interfaces in `domain/` (or `core-domain` for cross-module contracts). Zero Android/Compose dependency |
| **ViewModel** | `@HiltViewModel`, exposes state as `StateFlow` (see `kotlin.md`), no Compose imports, no `Context` unless via `@ApplicationContext` for a genuine platform need |
| **Composable (View)** | Reads `ViewModel` state, delegates actions back via method calls — no business logic, no direct repository/data-source access |

**Naming**: `<Feature>ViewModel` / `<Feature>Screen` for the top-level stateful composable.

**Forbidden in new MVVM code:**
- Business logic or direct data-source/repository calls in a Composable
- Android/Compose imports in a ViewModel or domain class
- A new screen bypassing the ViewModel to read a repository/use case directly

---

## Dependencies & singletons [Enforced]

- No hand-rolled singletons/service locators — Hilt scopes are the only DI mechanism
- Depend on interfaces (`core-domain`, or a module's own `domain/`), not concrete repository/data-source implementations, from any ViewModel or use case
- Cross-module contracts meant to be implemented differently across consumers (e.g. real vs. `fake`) belong in `core-ui` or `core-domain`, not duplicated per module
