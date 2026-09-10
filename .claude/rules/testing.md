# Testing Rules (mandatory) — Android/Kotlin tree

## Organisation [Enforced]

- **Unit tests**: `<module>/src/test/kotlin` — domain logic, mappers, repositories, ViewModels. Must run on the JVM without an Android device/emulator (Robolectric only if genuinely unavoidable)
- **Instrumented tests**: `<module>/src/androidTest` — Compose UI tests, anything requiring a real Android runtime. Present in `spotify-lib`, `app`, `core:logger`, `core:security`
- New ViewModels, use cases, repositories and mappers must be unit-testable in isolation — inject dependencies (interfaces from `core-domain` or the module's own `domain/`), never reach for a real Hilt graph or a real `SpotifyAppRemote` in a unit test; use `fake`'s fakes or MockK equivalents
- **Package root**: new `spotify-lib` unit tests go under `org.vander.spotifyclient` (matching main), not `com.vander.spotifyclient` — see the known mismatch in `kotlin.md`. Don't propagate it
- **Before considering a change done**: run the touched module's unit tests, e.g. `./gradlew :spotify-lib:testDebugUnitTest`, `./gradlew :core:security:testDebugUnitTest`, `./gradlew :app:testDebugUnitTest`. Run instrumented tests when the change touches Compose UI or Android runtime behavior
- `core-domain` and `core-dto` are plain JVM modules — their tests run with `./gradlew :core:domain:test` (no `DebugUnitTest` variant, no Android runtime involved)

## Test-first (mandatory for bug fixes) [Enforced]

1. Write a **failing test** that reproduces the bug before touching any implementation
2. Verify the test fails for the right reason
3. Fix the implementation
4. Verify the test passes and no existing tests regressed

## Verifiable success criteria

Define before starting any non-trivial implementation:
- What specific test passes?
- What observable behavior confirms correctness on device/emulator?
- What existing tests must still pass?

A feature is not complete until all criteria are explicitly verified.

## Tooling in this repo [Enforced — verified in `gradle/libs.versions.toml`]

- **Assertions/runner**: JUnit4 (`junit4 = "4.13.2"`)
- **Mocking**: MockK (`mockk` 1.13.10 for JVM, `mockk-android` 1.14.6 for instrumented) — not Mockito. See `spotify-lib/src/test/kotlin/.../FakePlayerStateRepository.kt` and `FakeSpotifySessionManager.kt` for existing fake-object style used alongside MockK
- **Flow**: Turbine (`app.cash.turbine`, 1.0.0) for `StateFlow`/`Flow` assertions
- **Coroutines**: `kotlinx-coroutines-test` (`runTest`)
- **Compose UI tests**: `androidx.compose.ui:ui-test-junit4` + `createComposeRule()`, MockK-android for instrumented mocks
- **Hilt tests**: `hilt-android-testing` + `kaptTest`/`kaptAndroidTest` on `hilt-compiler` — every module with Hilt (`spotify-lib`, `app`, `core-logger`, `core-security`) wires a matching `HiltTestRunner`/`testInstrumentationRunner` in its `build.gradle.kts`; check the module's actual runner class name before writing a new instrumented test (`spotify-lib` uses `org.vander.spotifyclient.HiltTestRunner`, others use `com.google.dagger.hilt.android.testing.HiltTestRunner` directly)

## Known unimplemented test doubles [do not treat as failing tests to fix silently]

`FakePlayerStateRepository`, `FakeLibraryRepository`, and `FakeSpotifySessionManager` in `spotify-lib/src/test/kotlin` have `TODO("Not yet implemented")` bodies on some methods — these are stubs for methods no current test exercises yet, not broken tests. If your task needs one of these methods to actually return data, implement it as part of that task; don't "fix" all of them speculatively.
