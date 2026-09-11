# 🧪 :fake

Fake ViewModel implementations, so `@Preview` and demo builds can render real screens
without a network or a Hilt graph.

---

## Overview

- Language: Kotlin (JDK 17), Android library
- Project deps: `:core:domain`, `:core:ui`
- Consumed by: `:app` (previews and demo paths)

Each fake implements the matching contract from `:core:ui` — `PlayerViewModel`,
`PlaylistViewModel`, `UserViewModel` — and serves static state. That is the whole point
of keeping those contracts in `core:ui` rather than in `app`: the real implementation
and the fake can be swapped at a preview's call site without either module knowing about
the other.

---

## Why previews need this

The Compose preview renderer runs inside the IDE, on Layoutlib. There is no Android
runtime, therefore no Hilt graph, no `hiltViewModel()`, no network. A preview that
reaches for a real ViewModel does not render. `:fake` is what makes a screen previewable
at all.

---

## Build & test

```bash
./gradlew :fake:assembleDebug
./gradlew :fake:testDebugUnitTest
```

---

## Known debt

`FakePlayerViewModel` has several `// TODO` comments where an action does not update
state. Expected for a preview fake — but do not assume they are wired up if what you are
building depends on fake state actually changing.
