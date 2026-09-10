# UI Rules (mandatory) — `app`

## Stack [Enforced]

Jetpack Compose + Material3 only — no XML layouts, no View system (the `res/values*/themes.xml` files exist only to satisfy the Android manifest theme attribute, not for actual UI).

## `core-ui` is NOT a design-system module here [Enforced]

Unlike a typical `core-ui`, `core:ui` in this repo holds **cross-module ViewModel contracts and UI state models** consumed by both the real app and `fake` (`PlayerViewModel`, `PlaylistViewModel`, `UserViewModel` interfaces, `UIQueueState`, `UIQueueItem`) — it has no theme, colors, or typography. Do not add Composables or design tokens to `core-ui` expecting it to behave like a shared design system; there isn't one yet.

The actual theme lives locally in the app: `app/.../designsystem/Theme.kt` (`AndroidAppTheme` composable). It is not shared with `spotify-lib` or any other module. If a task needs the theme reusable outside `app`, that's a real architectural change — propose it, don't do it silently.

## Package layout [Enforced]

`app` is organised **by feature**, not by layer. The old
`ui/screen/` + `ui/components/` split was abandoned: with ten screens coming,
adding one touched four sibling directories, and `components/` had become a
grab bag of four unrelated natures. There is no `ui/` level either — once
`feature/` exists it carries no information, and a feature directory holds its
ViewModel, so it is a vertical slice rather than UI.

```
org/vander/android/sample/
├─ AppRoot.kt                 root Box: content, bottom band, grain overlay
├─ navigation/                AppNavHost · AppNavGraph · AppBottomBar · NavItem
├─ designsystem/              Color · Type · Theme · VotDimens · Surfaces
│  ├─ modifier/               visual Modifier extensions
│  └─ component/              domain-free primitives
├─ component/                 shared across features, domain-aware
├─ feature/<name>/            screen + nav graph + ViewModel + own components
├─ util/                      composable helpers, constants
└─ di/                        Hilt modules and entry points
```

**Which directory does a composable go in?** One test, in order:

1. Does `grep -i spotify` on it come back empty, and does it take no domain
   type? → `designsystem/component/`
2. Is it used by two or more features? → `component/`
3. Otherwise → `feature/<name>/`

That is what puts `SpotifyTrackCover` in `component/` rather than the design
system: it builds a Spotify CDN URL, so it fails test 1, but `MiniPlayer` and
`PlaylistGrid` both use it, so it is not one feature's own.

**Naming**: `<Feature>Screen` for the top-level stateful composable of a
feature, `<Feature>NavGraph.kt` for its `NavGraphBuilder` extension.

**Previews** live next to their subject in the feature or design-system
directory — `feature/player/PreviewMiniPlayerWithLocalCover.kt`,
`designsystem/component/PreviewVinylLogoMark.kt`. There is no separate
`preview/` tree. They use the `fake` module's implementations or static sample
data, never a network-backed dependency.

**Forbidden**: a composable in `designsystem/` importing a domain type or a
ViewModel; a `feature/` directory reaching into another feature's package
(promote the shared piece to `component/` instead).

## Theming

- **[Enforced]** `AndroidAppTheme` (`app/.../designsystem/Theme.kt`) is the only theme in the tree — consume it, don't duplicate a second theme file elsewhere in `app`
- No hardcoded `Color(0xFF...)` or raw `.dp` magic numbers scattered across a screen — use `MaterialTheme` tokens, or add a new token to `designsystem/` (`VotDimens`, `Color.kt`) if it's meant to be reused

## Strings [Enforced — new code]

- No hardcoded text in a Composable you write or touch — every user-facing string goes through `stringResource(R.string.xxx)`, declared in `app/src/main/res/values/strings.xml`. This includes labels, button text, content descriptions, placeholders — not just body copy.
- Existing screens may still hardcode strings inline — do not migrate them as a side-effect of an unrelated task; report it instead if you notice inconsistency in a screen you're already touching
- Never introduce a new hardcoded string in a language different from the file's existing convention without flagging it

## State & recomposition

- Collect `StateFlow` with `collectAsStateWithLifecycle()` (not the plain `collectAsState()`, which ignores lifecycle) in new code
- Pass `data class`/`sealed interface` state down, not individual primitives, when more than 2-3 values move together
- Recomposition-cost and threading specifics (stable state, `LazyColumn` keys, image loading, Hilt constructor cost) should be reported as suggestions, not applied silently, unless the task explicitly asks for a performance pass — this repo has no dedicated performance-rules doc yet; call out concerns inline in your response instead
