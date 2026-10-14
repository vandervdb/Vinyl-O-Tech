# 🏛 :konsist

Architecture tests for the whole repository. This module contains no production code — it
is a JVM-only Kotlin module whose entire purpose is to read every other module's sources at
test time and fail loudly when one of them crosses a boundary it shouldn't.

```bash
./gradlew :konsist:test
```

---

## Overview

- Language: Kotlin (JVM), JDK 17 — no Android, no Gradle dependency on any other module
- Test framework: JUnit4 + [Konsist](https://konsist.lemonappdev.com/)
- Source access: `Konsist.scopeFromProduction()` parses every module's sources directly off
  disk. That's why `tasks.test` in `konsist/build.gradle.kts` declares the whole tree as an
  explicit input — Gradle has no dependency edge to tell it these files matter, and without
  that declaration a stale green result gets replayed instead of a real run.
- Because it depends on nothing, it can check the very edges — `spotify-lib` importing
  `app`, say — that a module can never violate simply by trying to compile.

## What's enforced

| Test | Rule |
|---|---|
| `CoreArchitectureTest` | `core:domain` imports neither Android, the Spotify SDK, nor DTOs · `core:security`'s `api/` imports neither its own `impl/`, Tink, nor DataStore |
| `SpotifyLibArchitectureTest` | `spotify-lib`'s domain layer is pure, and imports neither `data`, `bridge`, nor `di` |
| `AppArchitectureTest` | `app` depends on none of `spotify-lib`'s `data`, `domain`, or `di` packages |
| `AppViewModelTest` | a `@HiltViewModel` in `app` only receives types from `core:domain`, `core:logger`, or `core:ui` |
| `ContractPlacementTest` | an interface of `spotify-lib` used elsewhere belongs in `core:domain`; one used only inside `spotify-lib` is `internal` |
| `NamingConventionTest` | no `I` prefix, no `Impl` suffix |
| `LoggingConventionTest` | no `android.util.Log` in production code |
| `ArchitectureDebtTest` | the ratchet described below — not a rule about the codebase, a rule about the debt list itself |

`ArchitectureRules.kt` holds the shared predicates and scopes (`productionScope`,
`importsAny`, `spotifyLibInterfaces`, …) that the test classes above are built from.

---

## The debt ratchet

These rules were added to a codebase that already broke several of them — normal for a
project that grew feature by feature before anyone wrote the rule down. Two ways of
introducing a rule like that don't work: fixing every violation before merging the rule
blocks the rule on an unrelated rewrite, and turning it on as a plain assertion means it
just fails permanently and stops telling you anything about *new* damage.

`debt/ArchitectureDebt.kt` is the third option: an explicit, exhaustive list of every
declaration or file that violates a rule *today*. Each real test filters its scope down to
`filterNot { it in ArchitectureDebt.xxx }` before asserting `strict = true` — so the
assertion is strict everywhere except the named exceptions, and anything not on the list is
held to the rule immediately, day one, no grace period.

The list only being allowed to shrink is not a convention, it's enforced —
`ArchitectureDebtTest` re-checks every entry on every run:

- an entry that **no longer violates its rule** fails with *"no longer violates its rule:
  remove it from ArchitectureDebt"* — you fixed it and forgot to delete the line;
- an entry that was **renamed, moved, or deleted** fails with *"not found, renamed or
  deleted? Update ArchitectureDebt"* — the ratchet can't silently go stale in either
  direction.

Entries are typed per rule, and the shape matters — copy an existing entry of the same kind
rather than guessing:

| Set | Entries are | Matched by |
|---|---|---|
| `spotifyLibDomainImpure`, `appDependsOnSpotifyLibInternals`, `androidUtilLog`, `spotifyLibDomainWrongDirection` | project-relative file **paths** | `KoFileDeclaration.path.endsWith(entry)` |
| `contractsToMoveToCoreDomain`, `contractsToMakeInternal`, `interfacesWithIPrefix`, `classesWithImplSuffix` | fully-qualified **declaration names** | `fullyQualifiedName == entry` |
| `viewModelsDependingOnSpotifyLib` | bare **class names** | `name == entry` |

---

## Working with it

### Paying down an entry

1. Fix the violation (move the file, rename the type, change the import).
2. Delete its entry from the matching set in `ArchitectureDebt.kt` — same commit as the fix.
3. Run `./gradlew :konsist:test`. If the fix is incomplete, the *rule's own* test fails, not
   `ArchitectureDebtTest` — that tells you the fix didn't fully land, not that the bookkeeping
   is wrong.

### Introducing a new rule against an imperfect codebase

1. Write the test as if the codebase already complied (`strict = true`, no filtering).
2. Run it and read every failure — that list *is* the debt.
3. Populate the matching set in `ArchitectureDebt.kt` with exactly those entries.
4. Filter the debt out in the real test (`filterNot { it in ArchitectureDebt.xxx }`).
5. Add the matching case to `ArchitectureDebtTest`, mirroring an existing one of the same
   kind (path-based, FQN-based, or name-based) so the new list is held to "can only shrink"
   too.

### When something isn't really debt

A debt entry means "this is accepted for now and being tracked toward zero." It isn't a way
to permanently silence a rule for a case you think is wrong — if the rule itself doesn't fit
a situation, that's worth a conversation (and possibly a change to the rule), not an entry
that sits in `ArchitectureDebt.kt` forever.

---

## Gotchas

Found the hard way while writing these rules — see `docs/architecture/contracts-cleanup.md`
for the fuller history:

- **`assertFalse { a; b; c }` only returns `c`.** A multi-statement lambda isn't ANDed
  together; combine conditions with `||`, or collapse them into one expression (see
  `importsAny`).
- **`import.name == "android.*"` is never true.** There's no glob matching on import names —
  use `startsWith`.
- **An empty list passes both `assertFalse` and `assertTrue`.** Always pass `strict = true`
  when the check should fail on nothing to check. When an entire scope is debt today (see
  `AppViewModelTest`), assert `isNotEmpty()` on the *unfiltered* scope first — otherwise a
  rule with nothing left to check after filtering passes for the wrong reason.
- **A test in another module counts as "usage."** `ContractPlacementTest` reads
  `scopeFromProject()`, so a contract used only by an `app` test is already public in
  practice, not a false positive.
- **Only top-level interfaces are checked for placement.** A nested interface takes the
  visibility of its container (`RemoteConnector.RemoteListener` is not checked on its own).
- **`moduleName` looks like a path.** `"core/domain"`, not the Gradle path `":core:domain"`.
- **Konsist won't see source changes Gradle doesn't know about.** Without the `tasks.test`
  input declaration in `konsist/build.gradle.kts`, an edit in, say, `app` can make this
  module replay a stale green result instead of re-running.

---

## See also

- [`docs/architecture/contracts-cleanup.md`](../docs/architecture/contracts-cleanup.md) — why
  contracts live where they do, and the ongoing state of the cleanup this module's rules
  came out of.
- Root [`README.md`](../README.md) → *Quality gates* → *Architecture tests (Konsist)* — the
  short version, for anyone who isn't touching this module directly.
