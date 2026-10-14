# Nettoyage des contrats et tests d'architecture

Ce document rassemble les décisions, les règles et l'état du chantier mené sur la branche
`refactor/contracts-cleanup`. Il doit permettre de reprendre le travail sans l'historique de la
conversation qui l'a produit. Les constats ont été vérifiés dans le code ou par l'exécution des
tests, rien n'est supposé.

État après `62e30b3` : 15 tests Konsist verts, dette suivie par un cliquet. Option C terminée
(étapes 1 à 6), contrats des data sources passés en `internal` dans `data/`, ViewModels renommés.

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
port `PlayerController` : il est devenu `SpotifyPlayerController` dans `data/player/` (`f6ec31a`).

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

### Les contrats techniques restent dans leur module

Les contrats des data sources exposent des DTO et ne servent qu'à l'intérieur de `spotify-lib`.
Ils sont dans `data/remote/datasource/`, à côté de leurs implémentations, en `internal` et sans
préfixe `I` (`f6ec31a`).

| Contrat · `internal` | Implémentation · `internal` | Forme |
|---|---|---|
| `RemotePlaylistDataSource` | `SpotifyRemotePlaylistDataSource` | `fun interface` |
| `RemoteQueueDataSource` | `SpotifyRemoteQueueDataSource` | `fun interface` |
| `RemoteRecentlyPlayedDataSource` | `SpotifyRemoteRecentlyPlayedDataSource` | `fun interface` |
| `RemoteUserDataSource` | `SpotifyRemoteUserDataSource` | `fun interface` |
| `RemoteLibraryDataSource` | `SpotifyRemoteLibraryDataSource` | 3 méthodes |
| `RemoteAuthDataSource` | `SpotifyRemoteAuthDataSource` | interface |

- **Un ordre de mots unique** : `Remote<Sujet>DataSource` pour le contrat,
  `SpotifyRemote<Sujet>DataSource` pour l'implémentation, avec un `S` majuscule à `DataSource`.
- **Passer un contrat en `internal` entraîne tout ce qui l'expose** (règle « exposed visibility »
  de Kotlin) : un type public ne peut ni l'implémenter, ni le recevoir dans un constructeur public.
  Implémentations, repositories et modules Hilt ont suivi. Hilt l'accepte : `internal` n'existe que
  pour le compilateur Kotlin, la classe reste publique dans le bytecode.
- **`fun interface` quand le contrat n'a qu'une méthode** : les tests passent une lambda au lieu
  d'un mock (`SpotifyQueueRepository { Result.success(queueDto()) }`).
- **Un module qui lie et construit** met `@Binds` dans une `abstract class` et `@Provides` dans son
  `companion object` (`RemoteAuthModule`) : un `object` ne peut pas porter de fonction abstraite.

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

| Liste | `cda0099` | `62e30b3` | Ce qui reste | Identifiée par |
|---|---|---|---|---|
| `contractsToMakeInternal` | 14 | 5 | `AppRemoteProvider`, `RemoteConnector`, `IDataStoreManager`, `AuthClient`, `PlayerClient` | nom complet |
| `classesWithImplSuffix` | 11 | 2 | `KermitLoggerImpl`, `SpotifySessionManagerImpl` | nom complet |
| `interfacesWithIPrefix` | 10 | 3 | `IAuthRepository`, `ITokenProvider`, `IDataStoreManager` | nom complet |
| `spotifyLibDomainImpure` | 9 | 4 | `AppRemoteProvider`, `RemoteConnector`, `AuthClient`, `SpotifySessionManager` | chemin |
| `appDependsOnSpotifyLibInternals` | 7 | 4 | la session : `SpotifySessionEntryPoint`, deux ViewModels, `RememberSessionManager` | chemin |
| `contractsToMoveToCoreDomain` | 5 | 1 | `SpotifySessionManager` | nom complet |
| `viewModelsDependingOnSpotifyLib` | 5 | 2 | `SpotifyConnectionViewModel`, `SpotifyPlayerViewModel` | nom de classe |
| `androidUtilLog` | 2 | 2 | `PlaylistGrid`, `LifecycleObserverComponent` | chemin |
| `spotifyLibDomainWrongDirection` | 1 | 0 | — | chemin |

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
- **Un import vers une classe déplacée** ne donne pas « unresolved reference » mais une erreur kapt
  sur le `@Binds` (« parameter type must be assignable to the return type ») : kapt remplace la
  classe introuvable par un type d'erreur. Même remède : relire les modules DI.
- **Le dossier n'est pas le package.** Un fichier glissé dans un autre dossier garde sa ligne
  `package`, et Kotlin l'accepte. Konsist sélectionne par package : les data sources déplacées
  comptaient encore comme du domaine. Utiliser *Refactor → Move*, ou Alt+Entrée sur la ligne
  `package` soulignée.
- **Le cliquet suit les noms.** Un renommage fait réapparaître la dette comme une violation
  nouvelle, et l'ancienne entrée comme `not found`. On choisit : reporter l'entrée sous le nouveau
  nom, ou supprimer la dette. Chaque test du cliquet s'arrête à la première entrée introuvable.
- **Le hook pre-push teste le répertoire de travail**, pas seulement les commits : un fichier en
  cours d'édition peut faire échouer un push sans rapport avec lui.

---

## Option C : ports dans `core:domain`, adapters dans `spotify-lib`

Terminée.

| Port · `core:domain` | Adapter · `spotify-lib` | Visibilité |
|---|---|---|
| `playlist.PlaylistRepository` | `data.repository.SpotifyPlaylistRepository` | `internal` |
| `recent.RecentlyPlayedRepository` | `data.repository.SpotifyRecentlyPlayedRepository` | `internal` |
| `queue.QueueRepository` | `data.repository.SpotifyQueueRepository` | `internal` |
| `library.LibraryRepository` | `data.repository.SpotifyLibraryRepository` | `internal` |
| `user.UserRepository` | `data.repository.SpotifyUserRepository` | `internal` |
| `player.PlayerController` | `data.player.SpotifyPlayerController` | public |

Les ports sont rangés par feature (`org.vander.core.domain.<feature>`), à côté de leurs modèles,
et non dans un paquet `repository` commun : c'est déjà l'organisation de `core:domain`
(`player`, `state`, `auth`…). `QueueRepository` et `LibraryRepository` ont rejoint la liste en
cours de route.

1. ~~Règle de nommage dans Konsist.~~ Fait (`cda0099`).
2. ~~Déplacer les ports dans `core:domain`.~~ Fait : Playlist, RecentlyPlayed et Queue dans
   `43ba151`, `UserRepository` et `PlayerController` dans `8136ada`, `LibraryRepository` dans
   `f6ec31a`.
3. ~~Adapters `internal`, `PlayerUseCase` renommé.~~ Fait dans `f6ec31a` : repositories, data
   sources et leurs modules Hilt sont `internal`, `PlayerUseCase` est devenu
   `SpotifyPlayerController` dans `data/player/`. Un module Hilt qui fait `@Binds` vers une classe
   `internal` doit bien être `internal` lui aussi : le compilateur l'impose. `SpotifyPlayerController`
   est resté public.
4. ~~ViewModels sur les ports.~~ Fait : `SpotifyHomeViewModel` et `SpotifyPlaylistViewModel`
   reçoivent les repositories, appellent `refresh()` et loggent l'échec. Le flux ne passe pas par
   `null` : il démarre vide et garde sa dernière valeur en cas d'échec (voir le contrat plus haut).
5. ~~Supprimer les use cases relais.~~ Fait : `PlaylistUseCase`, `RecentlyPlayedUseCase`,
   `SpotifyRemoteUseCase`, `UseCaseModule` et leurs tests. Ce qu'ils couvraient est repris par
   `PlaylistRepositoryTest`, `RecentlyPlayedRepositoryTest` et `SpotifyQueueRepositoryTest`,
   à travers le port. Le log d'erreur est passé chez l'appelant.
6. ~~Mettre la dette à jour.~~ Fait à chaque commit, en suivant le cliquet.

| Dette | Avant (`cda0099`) | Objectif | Obtenu |
|---|---|---|---|
| Contrats publics hors de `core:domain` | 5 | 1 | **1** (`SpotifySessionManager`) |
| ViewModels dépendant de `spotify-lib` | 5 | 2 | **2** (`SpotifyConnectionViewModel`, `SpotifyPlayerViewModel`) |
| `app` → intérieur de `spotify-lib` | 7 | 4 | **4** |
| Suffixe `Impl` | 11 | 8 | **2** (`KermitLoggerImpl`, `SpotifySessionManagerImpl`) |
| Direction des couches | 1 | 0 | **0** |

Critères de réussite, vérifiés : `assembleDebug`, et les tests de tous les modules, Konsist compris,
passent. Chaque liste de dette a diminué, aucune entrée n'a été ajoutée.

## Ensuite

1. **Couche anti-corruption** pour `SpotifySessionManager` et `AuthClient`, qui exposent
   `Activity`, `Context` et `ActivityResultLauncher`. C'est ce qui garde deux ViewModels et quatre
   fichiers de `app` dans la dette, et le dernier contrat public hors de `core:domain`.
2. **Finir les contrats techniques** : `AppRemoteProvider`, `RemoteConnector`, `PlayerClient` et
   `IDataStoreManager` en `internal`, et `SpotifyPlayerController` aussi.
3. **Aligner `UserRepository`** sur `refresh(): Result<Unit>` : `fetchCurrentUser(): Unit` ne laisse
   voir un échec que dans les logs.
4. **Erreurs typées** : `refresh()` renvoie déjà un `Result<Unit>` ; l'étape suivante est une
   erreur `sealed` (réseau, session expirée, compte gratuit…) plutôt qu'une `Exception`.
5. **Préfixe `I` dans `core:domain`** : `IAuthRepository`, `ITokenProvider`.
6. **Modularisation par feature**, plus tard.

## Décisions ouvertes

- **Section « écoutés récemment » de l'accueil** : `HomeUiState` reçoit un champ `recentlyPlayed`,
  alimenté par `RecentlyPlayedRepository` dans `SpotifyHomeViewModel`. En cours, pas encore
  commité. Le bug de `a9254d9` (l'identifiant d'un morceau dans `playingPlaylistId`) est corrigé
  dans `43ba151` : le champ vient à nouveau du contexte de lecture.
- **Secret dans logcat** : `SpotifyRemoteAuthDataSource` logge en debug le Base64 de
  `CLIENT_ID:CLIENT_SECRET`, qui se décode directement. Masquer `CLIENT_SECRET` plus loin ne
  protège rien tant que cette ligne reste.
- ~~**Le nom des ViewModels `*Impl`**~~ Tranché : `Spotify<Feature>ViewModel`, sur le modèle des
  repositories (`62e30b3`). Le contrat (`core:ui`) nomme le rôle (`PlayerViewModel`),
  l'implémentation réelle sa source (`SpotifyPlayerViewModel`), le faux de `fake` le reste
  (`FakePlayerViewModel`). La règle est reportée dans `ui.md` et `architecture.md`.
- ~~**Hooks**~~ Résolu : Spotless ne passe que sur les fichiers indexés, le plugin ktlint autonome
  est retiré (`ab10834`), et les commits de la branche passent tous les hooks sans `--no-verify`.
  Reste la piste d'un pre-push plus léger, avec tous les contrôles en CI sur les pull requests
  vers `main`.
- **`.claude/rules/architecture.md`** : il décrit encore `core:security` comme non branché, et ne
  mentionne pas encore les règles Konsist.
