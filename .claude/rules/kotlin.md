# Kotlin Rules (mandatory — all code under `spotify-lib/`, `app/`, `core/*`)

## Conventions [Enforced]

- **English** identifiers everywhere — no French names
- **Comment language**: match the language already used in the file's comments; if none (or mixed), write in English. When touching a file with mixed-language comments, rewrite the comments of the lines you touch in English
- **Package root**: `org.vander.<module>` — `org.vander.spotifyclient` (`spotify-lib`), `org.vander.core.<name>` (`core/*`, e.g. `org.vander.core.security`), `org.vander.fake` (`fake`), `org.vander.android.sample` (`app`)
  - **Known inconsistency, do not propagate**: `spotify-lib/src/test/kotlin` uses `com.vander.spotifyclient` instead of `org.vander.spotifyclient` (main and `androidTest` both use `org.vander`). New unit tests in `spotify-lib` go under `org.vander.spotifyclient`, matching main — never copy the `com.vander` root into a new file. Don't silently rename the existing mismatched files unless that IS the task; report it instead
- **`val`** over `var` everywhere — a `var` must be justified by actual mutation, not convenience
- **Null-safety**: `?.`, `?:`, `let`/`takeIf` over explicit null checks. `!!` is forbidden except where nullability is provably impossible (add a one-line comment why). Prefer `sealed class`/`kotlin.Result` for error states over nullable returns that conflate "absent" and "failed"
- **Data classes** for all models, DTOs and UI/domain state — never a plain class with public mutable fields
- **Pattern matching**: prefer `when` with exhaustive `sealed class`/`sealed interface` branches over `if/else if` chains on type or state
- **Guard clauses**: early return over nested `if` blocks

```kotlin
// ❌
fun process(item: Item?) {
    if (item != null) {
        if (item.isValid) {
            doSomething(item)
        }
    }
}

// ✅
fun process(item: Item?) {
    if (item == null) return
    if (!item.isValid) return
    doSomething(item)
}
```

---

## Version Catalog discipline [Enforced]

Two custom root Gradle tasks enforce this — unlike ktlint (see below), they DO fail the build:

- Every dependency in a `dependencies {}` block must reference `libs.*` from `gradle/libs.versions.toml`. `./gradlew checkVersionHardcodedUsages` fails on a hardcoded `"group:artifact:version"` string
- Every version key declared in `libs.versions.toml` must be referenced somewhere. `./gradlew checkCatalogConsistency` fails on unused keys (a short allow-list exists for `android-compileSdk`/`minSdk`/`targetSdk`/`versionCode`/`versionName`)
- Local `.aar` files (`spotify-lib/libs/spotify-*.aar`) are a known, accepted exception — they can't go through the catalog

## Lint reality check [Enforced]

- **ktlint runs but does not gate anything**: root `build.gradle.kts` sets `ignoreFailures.set(true)` both at root and per-subproject. `lefthook.yml`'s pre-commit `ktlint` step runs `ktlintFormat` (auto-fixes, doesn't block). Don't rely on ktlint to catch a style issue — write clean code directly
- **Spotless** only enforces line endings (`LineEnding.UNIX`) and formatting via `ktlint(...)` under the hood, same `ignoreFailures` caveat — configuration cache is explicitly disabled for it (`notCompatibleWithConfigurationCache`, diffplug/spotless#987)
- The real gates are `./gradlew test`, `./gradlew lint`, `./gradlew assembleDebug` (all run at pre-push via lefthook) plus the two version-catalog tasks above

---

## Coroutines & Flow [Enforced]

- **Scope**: `viewModelScope` in ViewModels, `lifecycleScope`/`repeatOnLifecycle` in UI — never `GlobalScope`
- **Dispatchers**: `Dispatchers.IO` for network/disk (Ktor, DataStore, Tink), default (Main) for UI-touching code
- **State exposure**: `private val _x = MutableStateFlow(...)` + `val x = _x.asStateFlow()` — never expose a mutable `StateFlow`/`MutableSharedFlow` publicly
- **Suspend functions**: no `runBlocking` outside tests and app entry points

---

## Dependency Injection (Hilt) [Enforced]

`spotify-lib` already follows one-`@Module`-per-concern strictly (`di/AuthModule.kt`, `di/NetworkModule.kt`, `di/PlayerModule.kt`, `di/RepositoryModule.kt`, ... — 18 modules for one library module). Match this granularity in new code — do not add a second concern to an existing module file, create a new one.

- Bind interfaces to implementations with `@Binds` on an `abstract class`; use `@Provides` only for objects Hilt can't construct directly (Ktor `HttpClient`, `DataStore`, SDK clients, Tink primitives)
- Pick the narrowest `@InstallIn` scope that fits — do not default everything to `SingletonComponent`
- ViewModels: `@HiltViewModel` + `@Inject constructor(...)` — no manual `ViewModelProvider.Factory` unless Hilt genuinely can't cover the case
- `core-domain` and `core-dto` are pure Kotlin/JVM modules (no Android, no Hilt) — never add a Hilt/Android dependency to either; that boundary is what makes them usable as plain JVM libraries
- `core-security`'s DI is mid-refactor (`di/SecurityModule.kt` was removed in favor of `di/SecurityDataStoreProvider.kt`) and the module isn't wired into any consumer yet — check the current file layout before assuming its Hilt shape, don't pattern-match against `spotify-lib`'s modules for this one

---

## Error handling & logging [Enforced]

- Wrap external parsing/deserialization and remote calls in `runCatching`/`try-catch`, return `kotlin.Result<T>` or a domain sealed type — never let an exception escape a repository/data-source boundary silently
- **Logging goes through `core-logger`'s `Logger` interface (`org.vander.core.logger.Logger`, backed by Kermit via `KermitLoggerImpl`, injected via Hilt's `LoggerModule`) — never `android.util.Log` directly** in `spotify-lib`, `app`, or any `core/*` module that already depends on `core-logger`. A module without a `core-logger` dependency and a genuine reason not to add one is the only place `android.util.Log` is acceptable
- Every error path logs before returning or surfacing a failure — never fail silently

---

## Testing conventions used in this repo

- **Mocking**: MockK (`mockk`, `every`, `coEvery`) — not Mockito
- **Flow**: Turbine (`app.cash.turbine`, `.test { awaitItem() ... }`)
- **Coroutines**: `kotlinx-coroutines-test` (`runTest`)
- See `.claude/rules/testing.md` for test organisation
