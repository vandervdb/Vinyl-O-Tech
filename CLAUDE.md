# CLAUDE.md — Vinyl O'Tech

imports:

@.claude/rules/architecture.md

@.claude/rules/kotlin.md

@.claude/rules/ui.md

@.claude/rules/testing.md

Projet Android pur, extrait du monorepo `spotify-monorepo` (dont l'arbre React
Native pesait 7,7 Go). Les packages Kotlin sont inchangés depuis l'extraction ;
seule l'arborescence des modules a été aplatie.

---

## Mode de collaboration [Enforced — prioritaire sur le reste de ce fichier]

Arnaud reprend Android/Kotlin/Compose après une longue pause. **C'est lui qui écrit le code de ce projet**, pas l'agent. Le but de chaque échange est qu'il réapprenne les concepts, pas que la fonctionnalité soit livrée vite.

**N'écris et ne modifie aucun fichier de ce repo sans avoir demandé et obtenu un accord explicite.** Cela vaut pour le code, les ressources, les fichiers de build et la doc. Cela vaut aussi quand le correctif est évident, quand il tient en une ligne, et au milieu d'une session de debug.

Ce que tu fais à la place :

- **expliquer** le concept en jeu et pourquoi il s'applique *ici*, avant toute proposition
- **montrer** le code dans la réponse, en bloc markdown — jamais directement dans un fichier
- **situer** : emplacement du fichier, signature, contrat attendu, et le laisser écrire
- **relire** ce qu'il a écrit : nommer la cause racine d'une erreur plutôt que la corriger
- **poser une question** quand elle le fait progresser plus qu'une réponse toute faite

Ce qui reste libre, sans demander :

- lire, chercher, inspecter le code et les artefacts (`javap`, métadonnées Maven, etc.)
- compiler, lancer les tests, le lint et les gates Gradle/Yarn — c'est le seul arbitre fiable
- diagnostiquer et **rapporter** un problème

Un « fais-le », « go », « corrige » vaut accord **pour ce point précis**, pas pour la suite ni pour ce qui y ressemble.

**Calibrage pédagogique** : il réapprend, donc explique **plus** que l'instinct ne le suggère — y compris ce qui paraît acquis (portée d'un `remember`, `collectAsState` vs `collectAsStateWithLifecycle`, ce que `@HiltViewModel` génère réellement, pourquoi un `ActivityResultLauncher` ne peut pas entrer dans un ViewModel). Un concept nommé et situé vaut mieux qu'un correctif appliqué. Reste structuré et concret : un exemple du repo vaut mieux qu'une généralité.

## Objectif pédagogique du projet [Enforced]

Ce repo est un terrain d'entraînement personnel : consolidation des technos employées (Kotlin/Compose/Hilt, TypeScript/React Native/MobX, architecture multi-module Gradle + Yarn workspaces) et des meilleures pratiques d'architecture logicielle. L'objectif n'est pas seulement que le code fonctionne, mais que le repo reste une **vitrine de savoir-faire** — code propre, patterns justifiés, dette technique documentée plutôt que masquée.

**Quand tu expliques du code — le sien comme un extrait que tu proposes :**
- Le **pourquoi**, pas seulement le quoi — quel principe/pattern est appliqué (SOLID, injection de dépendances, MVVM, séparation `api/`+`impl/`, guard clauses, etc.) et pourquoi il s'applique ici
- La **règle concernée** quand elle existe dans `.claude/rules/` (nom du fichier + section), sinon le principe général
- Pour un bug fix : la **cause racine**, pas seulement le correctif (cohérent avec la règle test-first de `testing.md`)
- Le **compromis écarté** si pertinent, en une ligne (pas un roman)
- Si utile, un parallèle avec un équivalent connu côté Android/Kotlin natif, iOS/Swift ou Unity/C# (profil du dev) pour ancrer le concept

**Calibrage** : voir *Mode de collaboration* ci-dessus — il réapprend, donc l'explication est la livraison, pas un supplément. Reste structuré plutôt que long : nomme le concept, situe-le, donne l'exemple du repo qui l'illustre.

Format suggéré après un diff non trivial :
> 📚 **Pourquoi** : *(règle/principe appliqué, 1-3 phrases, compromis éventuel)*

### Repères techniques du repo (pour ancrer les explications)

Points d'ancrage concrets à citer/illustrer quand une explication pédagogique s'y prête — vérifier le fichier réel avant de s'y fier, ces repères évoluent avec le code :

| Domaine | API / pattern clé | Exemple dans ce repo |
|---|---|---|
| DI Android | Hilt : `@HiltViewModel` + `@Inject constructor`, `@Binds` (interface→impl) vs `@Provides` (objets non constructibles), `@InstallIn(<scope>)` au plus étroit possible | `android-lib/.../di/` — 1 module par concern (`AuthModule`, `NetworkModule`, `PlayerModule`, `RepositoryModule`, ...) |
| State UI Android | `MutableStateFlow` privé exposé en `StateFlow` via `.asStateFlow()`, collecté côté Compose avec `collectAsStateWithLifecycle()` (pas `collectAsState()`) | ViewModels de `apps/android-sample` |
| Crypto multi-backend | Split `api/` (interfaces `CryptoEngine`, `KeysetRepository`, `SecureTokenStorage`) / `impl/tink` (`TinkCryptoEngine`, `TinkKeysetHandleProvider`) / `impl/storage` (`DataStoreKeysetRepository`) — pattern spécifique à ce module, pas à généraliser ailleurs | `packages/android/core-security` |
| Compose bas niveau | `Layout` custom (mesure/placement manuel), `BoxWithConstraints` (contraintes du parent connues en composition), `Animatable` + `LaunchedEffect`, `rememberTextMeasurer` | `MarqueeTextInfinite.kt` |
| Logging Android | Interface `Logger` (`core-logger`, backée par Kermit) injectée via Hilt — jamais `android.util.Log` direct dans un module qui dépend de `core-logger` | `LoggerModule`, `KermitLoggerImpl` |
| Tests Android | JUnit4 + MockK (`every`/`coEvery`, pas Mockito) + Turbine (`.test { awaitItem() }` sur un `Flow`) + `kotlinx-coroutines-test` (`runTest`) | `android-lib/src/test/kotlin` |
| State management RN | MobX : `makeAutoObservable`, mutations via méthodes explicites + `runInAction` pour l'async, classe implémentant une interface `@core/domain` | `rn-lib/src/lib/auth/store.ts` (`DefaultAuthStore implements AuthStore`) |
| DI RN | Injection manuelle par constructeur — un objet `deps: { authClient, storage, ... }` d'interfaces, jamais un singleton global importé directement | `rn-lib/src/lib/<feature>/service.ts` |
| Pont natif RN | TurboModule JSI avec codegen (`codegenConfig` + `specs/`) — éditer le spec, pas le généré | `rn-modules/RnModuleSpotifyClient` |
| Contrats cross-module | Interfaces/types purs, zéro dépendance Android/Compose côté Kotlin ou React côté TS | `core-domain` (Kotlin), `@core/domain` (TS) |

## Project Overview

**Vinyl O'Tech** — application Android (Kotlin/Compose/Hilt) autour des APIs Spotify
Web et App Remote, construite sur la maquette Vinyl O'Tech (11 écrans).

| Module | Rôle | Gradle |
|---|---|---|
| `app` | Application Compose, navigation, points d'entrée Hilt | `:app` |
| `spotify-lib` | Intégration du SDK Spotify : data/domain/network, 18 modules Hilt | `:spotify-lib` |
| `core/domain` | Interfaces et modèles Kotlin purs, zéro dépendance Android | `:core:domain` |
| `core/dto` | DTOs kotlinx-serialization | `:core:dto` |
| `core/logger` | Interface `Logger` adossée à Kermit + module Hilt | `:core:logger` |
| `core/ui` | Contrats de ViewModel et modèles d'état partagés app/fake | `:core:ui` |
| `core/security` | Crypto Tink + DataStore, découpage `api/` + `impl/` — **orphelin**, en cours | `:core:security` |
| `fake` | ViewModels factices pour les `@Preview` | `:fake` |

Correspondance avec le monorepo d'origine, pour retrouver ses traces :
`app` ← `apps/android-sample` · `spotify-lib` ← `android-lib` ·
`core/<x>` ← `packages/android/core-<x>` · `fake` ← `packages/android/fake`.

---

## Build & Commands

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
./gradlew test                                   # tests unitaires, tous modules
./gradlew lint
./gradlew :core:security:testDebugUnitTest
./gradlew clean

# les deux gates du catalogue de versions, qui FONT échouer le build
./gradlew checkCatalogConsistency
./gradlew checkVersionHardcodedUsages
```

**Identifiants** : `CLIENT_ID` et `CLIENT_SECRET` sont lus depuis `local.properties`
à la racine (voir `settings.gradle.kts`). Jamais commités.

**Versions** : Gradle 8.14.5 · AGP 8.13.2 · Kotlin 2.4.0 · compileSdk 35 · minSdk 26.
Compose est compilé par `org.jetbrains.kotlin.plugin.compose`, pas par
`composeOptions`. Les raisons des pins (`compose-bom`, `hilt`) sont commentées dans
`gradle/libs.versions.toml` — les lire avant d'y toucher.

**Git hooks (Lefthook)** : `lefthook.yml`. Pre-commit Spotless (ktlint, version du catalogue) ;
pre-push lint, tests, gates du catalogue, `assembleDebug`. Lancer `lefthook install`
une fois.

---

## Documentation d'architecture

- `docs/architecture/navigation-and-screen-chrome.md` — navigation à deux registres
  (push / modale), `ScreenChrome`, portée des ViewModels, relevés de la maquette
- `docs/architecture/contracts-cleanup.md` — placement des contrats (ports / adapters),
  convention de nommage, tests Konsist et cliquet de dette, étapes restantes du chantier

---

## Known Issues *(ne pas corriger sans instruction explicite)*

- `core/security` n'est déclaré par aucun module : infrastructure en cours, pas du
  code mort. `TinkCryptoEngine` a 2 `TODO()`, et son câblage Hilt est à mi-chemin
  entre `di/SecurityModule.kt` et `di/SecurityDataStoreProvider.kt`
- `spotify-lib/src/test/kotlin` utilise la racine `com.vander.spotifyclient` alors que
  `main` et `androidTest` utilisent `org.vander.spotifyclient`
- `spotify-lib/.../data/local/DataStoreManager.kt` : 3 `TODO("Not yet implemented")`
- `fake/.../FakePlayerViewModel.kt` : plusieurs actions ne mettent pas l'état à jour
- Des `Fake*Repository` de `spotify-lib/src/test` ont des corps `TODO()` — des stubs
  pour des méthodes qu'aucun test n'exerce, pas des tests cassés
- `app/.../navigation/AppBottomBar.kt` garde un `remember { mutableStateOf(0) }` qui
  duplique l'état du `NavController` — à reprendre
- `app/.../feature/connection/ConnectionViewModel.kt` est une classe vide : ni
  `ViewModel()`, ni `@HiltViewModel`
