# 📱 :app

The Compose application: navigation, screens, design system, Hilt entry points.

---

## Overview

- Language: Kotlin (JDK 17)
- UI: Jetpack Compose + Material 3 — no XML layout anywhere. `res/values*/themes.xml`
  exists only to satisfy the manifest's `android:theme`, not to style anything
- DI: Hilt, plus `@EntryPoint` accessors where a composable needs a singleton
  (`util/RememberSessionManager.kt` is the pattern to copy)
- Project deps: `:spotify-lib`, `:core:domain`, `:core:logger`, `:core:ui`, `:fake`

---

## Layout

Organised **by feature, not by layer**. There is no `ui/` level: once `feature/` exists
it carries no information, and a feature directory holds its own ViewModel, so it is a
vertical slice rather than UI.

```
src/main/kotlin/org/vander/android/sample/
├─ AppRoot.kt                 root Box: content, grain overlay, snackbar host
├─ navigation/                AppNavHost · AppBottomBar · NavItem
├─ designsystem/              Color · Type · Theme · VotDimens · Surfaces
│  ├─ modifier/               visual Modifier extensions
│  └─ component/              domain-free primitives
├─ component/                 shared across features, domain-aware
├─ feature/<name>/            screen + nav graph + ViewModel + own components
├─ util/                      composable helpers, constants
└─ di/                        Hilt modules and entry points
```

**Which directory does a composable go in?** One test, in order:

1. Does `grep -i spotify` come back empty, and does it take no domain type?
   → `designsystem/component/`
2. Is it used by two or more features? → `component/`
3. Otherwise → `feature/<name>/`

That is what puts `SpotifyTrackCover` in `component/` rather than the design system: it
builds a Spotify CDN URL, so it fails test 1, but `MiniPlayer` and `PlaylistGrid` both
use it, so it is not one feature's own.

Previews live next to their subject — there is no separate `preview/` tree. They use
`:fake` or static sample data, never a network-backed dependency: the preview renderer
has no Android runtime, so no Hilt graph and no network.

---

## Theme

`designsystem/Theme.kt` holds `AndroidAppTheme`, the only theme in the tree. It is
**dark-only** by design — a fixed brand identity, like Spotify's own. There is no
`darkTheme` parameter and no dynamic colour: a light palette would be a palette to
design, not a boolean to flip.

Typography uses downloadable Google Fonts (Space Grotesk for titles, DM Sans for body),
so no font file ships in the APK. A `FontFamily` serves the *nearest declared* weight
rather than synthesising one, so a weight used by a slot must be declared in the family
— a missing face is silent, not an error.

---

## Build & test

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedAndroidTest     # Hilt test runner, device required
```

Credentials come from `local.properties` at the repo root — see the root README.
