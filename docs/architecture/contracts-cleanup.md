# Nettoyage des contrats et tests d'architecture

Ce document rassemble les décisions, les règles et l'état du chantier mené sur la branche
`refactor/contracts-cleanup`. Il doit permettre de reprendre le travail sans l'historique de la
conversation qui l'a produit. Les constats ont été vérifiés dans le code ou par l'exécution des
tests, rien n'est supposé.

État après `43ba151` : 15 tests Konsist verts, dette suivie par un cliquet. Option C faite
aux étapes 1, 2, 4, 5 et 6 ; reste l'étape 3.

---

## Le problème de départ

Le dépôt exprimait une même idée (séparer le contrat de l'implémentation) de trois façons :

| Où | Séparation | Vocabulaire |
|---|---|---|
| `spotify-lib` | par couches | `domain/` et `data/` |
| `core:domain` ↔ `spotify-lib` | par module Gradle | le contrat dans un module, l'implémentation dans l'autre |
| `core:security` | par technologie | `api/` et `impl/<tech>/` |

En plus, `spotify-lib/domain/` contenait des contrats techniques qui exposaient Android, le SDK
Spotify ou des DTO, et `app` importait directement l'intérieur de `spotify-lib`.

## Les décisions

### Placer chaque contrat selon qui l'utilise

| Contrat | Où | Visibilité | Pur ? |
|---|---|---|---|
| Utilisé par un autre module (un **port**) | `core:domain` (ou `api/` d'un module core) | public | oui, obligatoirement |
| Technique, utilisé seulement dans son module (datasource, SDK, stockage) | `data/`, à côté de son implémentation | `internal` | non |
| Port du domaine utilisé seulement dans son module | reste dans `domain/` | `internal` | oui |

La **couche** (`domain` ou `data`) dépend de ce dont le contrat a besoin. La **visibilité**
(`public` ou `internal`) dépend de qui l'utilise. Et une signature publique rend publics les types
qu'elle expose : un constructeur public ne peut pas recevoir un type `internal`.

### Convention de nommage

Un nom dit ce que le type, le package ou le module ne disent pas déjà.

| Rôle | Le nom dit | Exemple |
|---|---|---|
| Port | le rôle métier | `PlaylistRepository` |
| Adapter | sa technologie ou sa source | `SpotifyPlaylistRepository` |
| Faux (tests) | la même chose | `FakePlaylistRepository` |

- Pas de suffixe `Impl` : `: PlaylistRepository` dit déjà que la classe est une implémentation.
- Pas de préfixe `I` : c'est une convention C#, pas Kotlin.
- `Spotify` va dans le nom de l'adapter, qu'il distingue d'un faux. Dans celui du port, il ne
  distingue rien.

### Pas de use case qui ne fait que relayer

Un use case se justifie quand il porte une règle métier réutilisée ou complexe. `PlaylistUseCase`,
`RecentlyPlayedUseCase` et `SpotifyRemoteUseCase` recopiaient dans leur propre `StateFlow` un
résultat que le repository gardait déjà : ils sont supprimés (`43ba151`), et les ViewModels
dépendent directement des ports. `PlayerUseCase` n'est pas un use case mais l'adapter Spotify du
port `PlayerController` : il sera renommé et déplacé, pas supprimé.

### Le contrat d'un repository qui garde un état

```kotlin
interface PlaylistRepository {
    val playlists: StateFlow<PlaylistCollection>
    suspend fun refresh(): Result<Unit>
}
```

- **La donnée ne passe que par le `StateFlow`**, seule source de vérité. `refresh()` ne la
  renvoie pas : deux canaux pour la même donnée poussaient chaque consommateur à la recopier.
- **`refresh()` renvoie `Result<Unit>`** : il dit seulement si l'opération a réussi. Un appelant
  qui doit réagir à l'échec le peut, les autres l'ignorent. `Unit` seul perdrait l'erreur
  (c'est encore le cas de `UserRepository.fetchCurrentUser()`).
- **Un échec garde la dernière valeur publiée** : hors ligne, l'écran garde ce qu'il avait au lieu
  de se vider.
- **Le flux démarre à une valeur vide**, pas à `null`, sauf pour la queue (`CurrentlyPlaying?`,
  où `null` veut dire « pas encore chargée »).
- **L'adapter relance `CancellationException`** au lieu de la convertir en `Result.failure` :
  `catch (e: Exception)` l'attraperait, et un ViewModel détruit pendant l'appel continuerait.
- **Le `@Binds` de l'adapter est `@Singleton`** : l'état vit dans le repository, une instance par
  injection donnerait un état par consommateur.
- **L'appelant logge l'échec** (`.onFailure { logger.e(...) }`), puisque l'adapter ne le fait pas.
  Un `getOrThrow()` dans un `collect {}` est interdit : l'exception tue le collecteur pour le
  reste de la session.

### Nettoyages faits

- Le pont React Native (`bridge/`, `PlayerStateDto` et son mapper) est supprimé : son client
  n'existe plus. `AuthConfigK` est passé dans `domain/auth`.
- Les dossiers `core/domain/bin` et `core/dto/bin` (des copies de sources laissées par l'ancien
  projet React Native) sont supprimés. Konsist les analysait comme du code de production.
- Une seule instance `Json` pour `spotify-lib` : `spotifyJson` dans `utils/HttpResponseParser.kt`,
  en `@PublishedApi internal` parce que les fonctions `inline` publiques qui l'utilisent sont
  recopiées chez l'appelant. Une instance par appel perdait le cache des sérialiseurs.

---

## Les tests d'architecture (`:konsist`)

Module JVM qui ne contient que des tests. Il lit les sources de tout le projet sans dépendre
d'aucun module.

```bash
./gradlew :konsist:test
```

| Classe | Règle |
|---|---|
| `CoreArchitectureTest` | `core:domain` n'importe ni Android, ni SDK Spotify, ni DTO · l'`api/` de `core:security` n'importe ni `impl/`, ni Tink, ni DataStore |
| `SpotifyLibArchitectureTest` | le domaine de `spotify-lib` est pur, et il n'importe ni `data`, ni `bridge`, ni `di`, ni `network` |
| `AppArchitectureTest` | `app` ne dépend ni de `data`, ni de `domain`, ni de `di` de `spotify-lib` |
| `AppViewModelTest` | un `@HiltViewModel` de `app` ne reçoit que des types de `core/domain`, `core/logger` ou `core/ui` (une liste d'autorisations) |
| `ContractPlacementTest` | une interface de `spotify-lib` importée ailleurs doit aller dans `core:domain` · une interface utilisée seulement dans `spotify-lib` est `internal` |
| `NamingConventionTest` | pas de préfixe `I`, pas de suffixe `Impl` |
| `LoggingConventionTest` | pas d'`android.util.Log` en production |
| `debt/ArchitectureDebtTest` | le cliquet (voir ci-dessous) |

### Travailler avec la dette

`debt/ArchitectureDebt.kt` liste ce qui viole une règle aujourd'hui. Chaque liste ne peut que
diminuer :

- une violation nouvelle, qui n'est pas listée, fait échouer la règle ;
- une entrée listée qui ne viole plus sa règle fait échouer `ArchitectureDebtTest` ;
- un fichier ou une déclaration renommé ou déplacé fait échouer le cliquet avec `not found`.

On retire l'entrée dans le commit qui corrige la violation. Il suffit de suivre les messages du
cliquet.

| Liste | `cda0099` | Maintenant | Identifiée par |
|---|---|---|---|
| `contractsToMakeInternal` | 14 | 12 | nom complet |
| `classesWithImplSuffix` | 11 | 7 | nom complet |
| `interfacesWithIPrefix` | 10 | 10 | nom complet |
| `spotifyLibDomainImpure` | 9 | 9 | chemin |
| `appDependsOnSpotifyLibInternals` | 7 | 4 | chemin |
| `contractsToMoveToCoreDomain` | 5 | 1 | nom complet |
| `viewModelsDependingOnSpotifyLib` | 5 | 2 | nom de classe |
| `androidUtilLog` | 2 | 2 | chemin |
| `spotifyLibDomainWrongDirection` | 1 | 1 | chemin |

### Pièges rencontrés

- **Une lambda `assertFalse { a; b; c }` ne renvoie que `c`.** Il faut combiner avec `||`, ou
  passer par une seule expression (`importsAny(prefixes)`).
- **`import.name == "android.*"` n'est jamais vrai.** L'étoile n'est pas un motif, il faut utiliser
  `startsWith`.
- **Une liste vide passe `assertFalse` / `assertTrue`.** Il faut `strict = true`. Quand toute la
  liste est dans la dette, on vérifie qu'elle n'est pas vide avant le filtre (`AppViewModelTest`).
- **Gradle ne voit pas les sources lues par Konsist.** Elles sont déclarées comme entrées de la
  tâche `test` dans `konsist/build.gradle.kts`, sinon un ancien résultat vert est réaffiché.
- **Les tests des autres modules comptent comme usage** (`ContractPlacementTest` lit
  `scopeFromProject()`) : un test de `app` qui utilise un contrat le garde public.
- **Seules les interfaces de premier niveau sont vérifiées** pour le placement : une interface
  imbriquée prend la visibilité de celle qui la contient (`RemoteConnector.RemoteListener`).
- **`moduleName` a la forme d'un chemin** : `"core/domain"`, pas `":core:domain"`.
- **Après un refus de commande dans Claude Code**, vérifier `git status` : deux commandes refusées
  se sont quand même exécutées en partie pendant ce chantier.
- **Un renommage de l'IDE peut produire un `@Binds X → X`.** En renommant `SpotifyQueueRepositoryImpl`
  en `SpotifyQueueRepository` (le nom de l'ancien port), Rider a changé les deux côtés du `@Binds`.
  Le port n'était plus fourni, et l'erreur n'apparaît qu'à la génération du graphe par kapt dans
  `:app`. Relire le module DI après chaque renommage.
- **Rider signale à tort qu'un type de coroutines « ne sera pas accessible »** en déplaçant un port
  dans `core:domain`, alors que le module déclare `api(libs.kotlinx.coroutines.core)`. Le
  compilateur Gradle fait foi.
- **« Apply Changes » après un déplacement de classes** laisse des dex incohérents dans
  `code_cache/.overlay` (`NoClassDefFoundError` au démarrage). Désinstaller et réinstaller.

---

## Option C : ports dans `core:domain`, adapters dans `spotify-lib`

Étapes 1, 2, 4, 5 et 6 faites. Reste l'étape 3 : passer les adapters en `internal` et renommer
`PlayerUseCase`.

| Port · `core:domain` | Adapter · `spotify-lib` | État |
|---|---|---|
| `playlist.PlaylistRepository` | `data.repository.SpotifyPlaylistRepository` | déplacé, renommé, pas `internal` |
| `recent.RecentlyPlayedRepository` | `data.repository.SpotifyRecentlyPlayedRepository` | déplacé, renommé, pas `internal` |
| `queue.QueueRepository` | `data.repository.SpotifyQueueRepository` | déplacé, renommé, pas `internal` |
| `user.UserRepository` | `data.repository.SpotifyUserRepository` | déplacé, pas `internal` |
| `player.PlayerController` | `domain.usecase.PlayerUseCase` → `SpotifyPlayerController` dans `data/player/` | port déplacé, adapter à renommer |

Les ports sont rangés par feature (`org.vander.core.domain.<feature>`), à côté de leurs modèles,
et non dans un paquet `repository` commun : c'est déjà l'organisation de `core:domain`
(`player`, `state`, `auth`…).

`QueueRepository` a rejoint la liste : `SpotifyRemoteUseCase` supprimé, `PlayerUseCase` dépend
directement du port.

1. ~~Règle de nommage dans Konsist.~~ Fait (`cda0099`).
2. ~~Déplacer les ports dans `core:domain`.~~ Fait : les trois premiers dans `43ba151`,
   `UserRepository` et `PlayerController` ensuite. `UserRepository` garde pour l'instant
   `fetchCurrentUser(): Unit`, qui perd l'erreur : il reste à l'aligner sur `refresh(): Result<Unit>`.
3. **Adapters `internal`, et `PlayerUseCase` renommé** en `SpotifyPlayerController` dans
   `data/player/`. À vérifier sur un premier adapter : un module Hilt qui fait `@Binds` vers une
   classe `internal` doit être `internal` lui aussi (`SpotifyPlaylistModule` l'est déjà).
4. ~~ViewModels sur les ports.~~ Fait : `HomeViewModelImpl` et `PlayListViewModelImpl` reçoivent
   les repositories, appellent `refresh()` et loggent l'échec. Le flux ne passe pas par `null` :
   il démarre vide et garde sa dernière valeur en cas d'échec (voir le contrat plus haut).
5. ~~Supprimer les use cases relais.~~ Fait : `PlaylistUseCase`, `RecentlyPlayedUseCase`,
   `SpotifyRemoteUseCase`, `UseCaseModule` et leurs tests. Ce qu'ils couvraient est repris par
   `PlaylistRepositoryTest`, `RecentlyPlayedRepositoryTest` et `SpotifyQueueRepositoryTest`,
   à travers le port. Le log d'erreur est passé chez l'appelant.
6. ~~Mettre la dette à jour.~~ Fait, en suivant le cliquet.

| Dette | Avant (`cda0099`) | Objectif | Maintenant |
|---|---|---|---|
| Contrats publics hors de `core:domain` | 5 | 1 | **1** (`SpotifySessionManager`) |
| ViewModels dépendant de `spotify-lib` | 5 | 2 | **2** (`ConnectionViewModelImpl`, `PlayerViewModelImpl`) |
| `app` → intérieur de `spotify-lib` | 7 | 4 | **4** |
| Suffixe `Impl` | 11 | 8 | **7** |
| Direction des couches | 1 | 0 | 1 (`PlayerUseCase`, étape 3) |

Critères de réussite, vérifiés : `assembleDebug`, et les tests de tous les modules, Konsist compris,
passent. Chaque liste de dette a diminué ou est restée stable, aucune entrée n'a été ajoutée.

## Ensuite

1. **Finir l'étape 3** de l'option C.
2. **Couche anti-corruption** : `SpotifySessionManager` expose `Activity`, `Context` et
   `ActivityResultLauncher`. C'est le dernier contrat public à nettoyer avant qu'il aille dans
   `core:domain`.
3. **Erreurs typées** : `refresh()` renvoie déjà un `Result<Unit>` ; l'étape suivante est une
   erreur `sealed` (réseau, session expirée, compte gratuit…) plutôt qu'une `Exception`.
4. **Contrats techniques** dans `data/` en `internal`, avec renommage des `I*` au passage.
5. **Modularisation par feature**, plus tard.

## Décisions ouvertes

- **Section « écoutés récemment » de l'accueil** : `HomeViewModelImpl` rafraîchit
  `RecentlyPlayedRepository` et combine son flux, mais `HomeUiState` n'a pas encore de champ pour
  lui (le paramètre est ignoré par `_`). Soit on ajoute le champ, soit on retire le flux tant que la
  section n'est pas branchée. Le bug de `a9254d9` (l'identifiant d'un morceau dans
  `playingPlaylistId`) est corrigé dans `43ba151` : le champ vient à nouveau du contexte de lecture.
- **Le nom des ViewModels `*Impl`** : ils implémentent les contrats de `core:ui`, tout comme ceux
  de `fake`. Or `ui.md` réserve `<Feature>ViewModel`, déjà pris par l'interface. Il faudra
  trancher. En attendant, ils restent dans la dette.
- **Hooks et CI** : Spotless ne passe plus que sur les fichiers indexés, et le plugin ktlint
  autonome est retiré (`ab10834`) : les commits et le push de `43ba151` sont passés par tous les
  hooks, sans `--no-verify`. Reste la piste d'un pre-push plus léger, avec tous les contrôles en CI
  sur les pull requests vers `main`.
- **`.claude/rules/architecture.md`** : il décrit encore `core:security` comme non branché, et ne
  mentionne pas encore les règles Konsist.
