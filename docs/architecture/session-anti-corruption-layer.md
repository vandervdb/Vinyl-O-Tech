# Couche anti-corruption de la session

Explication du chantier décrit en section 3 de `refactors-2026-09-28.md` : ce qu'il vise,
pourquoi, et comment chaque étape s'y rattache. Le plan détaillé reste dans ce document-là ;
celui-ci en donne le raisonnement.

---

## 1. Le concept

Une **couche anti-corruption** (*Domain-Driven Design*, Eric Evans) s'intercale entre le domaine
et un système extérieur dont le modèle ne lui convient pas. Elle traduit dans les deux sens, pour
que le vocabulaire de l'extérieur ne se diffuse pas dans le domaine.

Ici, deux systèmes extérieurs :

- **le framework Android** : `Activity`, `Context`, `ActivityResultLauncher`, `ActivityResult` ;
- **le SDK Spotify Auth** : `AuthorizationResponse` et ses types `CODE`, `TOKEN`, `ERROR`.

Le domaine ne devrait connaître que des notions métier : la session est prête, une autorisation
est requise, l'utilisateur a annulé.

---

## 2. Le problème aujourd'hui

Le contrat `SessionManager` (`spotify-lib/.../domain/session/SessionManagerB.kt`) expose la
plomberie Android :

```kotlin
suspend fun requestAuthorization(
    launchAuth: ActivityResultLauncher<Intent>,
    activity: Activity,
    context: Context,
    coroutineScope: CoroutineScope,
    dispatcher: CoroutineDispatcher,
    config: AuthConfigK?,
)
fun handleAuthResult(context: Context, result: ActivityResult, coroutineScope: CoroutineScope, dispatcher: CoroutineDispatcher)
```

Conséquences, toutes visibles dans le cliquet Konsist (`konsist/.../debt/ArchitectureDebt.kt`) :

| Conséquence | Entrée de dette |
|---|---|
| Le contrat ne peut pas monter dans `core:domain`, module JVM pur | `contractsToMoveToCoreDomain` |
| Le `domain/` de `spotify-lib` importe Android | `spotifyLibDomainImpure` (`SessionManagerB.kt`, `AuthClient.kt`) |
| Les ViewModels dépendent de `spotify-lib` pour l'utiliser | `viewModelsDependingOnSpotifyLib` |
| `app` importe des internes de `spotify-lib` | `appDependsOnSpotifyLibInternals` |

S'y ajoutent deux effets concrets :

- **un ordre d'appels imposé** — enregistrer le launcher, lancer, relayer le résultat —, que
  l'appelant doit connaître (le KDoc de `SpotifySessionManager` le dit : *the flow is split
  across three calls*) ;
- **pas de double possible dans `fake`**, donc pas de `@Preview` de l'écran de connexion dans tous
  ses états.

---

## 3. La cible

### 3.1 Un contrat pur

```kotlin
// core:domain
interface SessionManager {
    val sessionState: StateFlow<SessionState>
    suspend fun resumeSession()
    suspend fun completeAuthorization(authorizationCode: String)
    suspend fun shutDown()
    suspend fun signOut()
}
```

Chaque dépendance Android disparaît d'une façon précise :

| Aujourd'hui en paramètre | Devient |
|---|---|
| `Context` | pas d'`@ApplicationContext` possible pour l'App Remote : `SpotifyAppRemote.connect` exige une `Activity` (§ 3.4) |
| `CoroutineScope`, `CoroutineDispatcher` | le `@ApplicationScope` existant (`ApplicationScopeModule`) : une session vit plus longtemps qu'un écran |
| `Activity`, `ActivityResultLauncher` | sortent du contrat : le lancement appartient à l'UI (§ 3.2) |
| `ActivityResult` | réduit à son sens métier : un code, via `completeAuthorization(code)` |

**Flux piloté par l'état.** Sans token, le manager publie `SessionState.AuthorizationRequired` ;
l'UI observe l'état et lance l'autorisation en réaction. Le contrat dit ce dont il a besoin au
lieu d'imposer une séquence d'appels.

**`resumeSession()` corrige aussi un bug existant.** Aujourd'hui `AppRoot` appelle `shutDown()`
sur `ON_STOP`, qui déconnecte l'App Remote, et rien ne reconnecte sur `ON_START` :
`playerApi` reste `null` au retour au premier plan. `resumeSession()` est la moitié manquante —
reconnecter si un token existe, publier `AuthorizationRequired` sinon. Reconnecter demande une
`Activity` : qui la fournit, et donc qui déclenche `resumeSession()`, dépend de l'option retenue
en § 3.4.

### 3.2 `SpotifyAuthorizationContract`, la frontière

Android fournit déjà l'abstraction : `ActivityResultContract<I, O>`, avec `createIntent` et
`parseResult`. L'interface `AuthClient` en est une réinvention, avec un callback en plus.

```kotlin
class SpotifyAuthorizationContract : ActivityResultContract<AuthorizationOptions, AuthorizationOutcome>() {
    override fun createIntent(context: Context, input: AuthorizationOptions): Intent
    override fun parseResult(resultCode: Int, intent: Intent?): AuthorizationOutcome
}
```

- **La traduction est à un seul endroit.** `parseResult` convertit la réponse Spotify en
  `AuthorizationOutcome` ; le SDK ne sort pas de ce fichier.
- **Plus de callback.** `rememberLauncherForActivityResult(contract) { outcome -> … }` livre un
  résultat typé.
- **`createIntent` exige une `Activity`**, car `createLoginActivityIntent(Activity, …)` la
  demande : `context as? Activity ?: error(...)`. Une `ComponentActivity` passe elle-même.
- **Couture de test.** `AuthorizationResponse.Builder` n'est pas utilisable en test JVM. La
  logique va dans une fonction pure `toOutcome(type, code, error)` ; `parseResult` extrait les
  trois champs et l'appelle.
- **Bug corrigé au passage** : une réponse `TOKEN` est aujourd'hui renvoyée comme un code, et
  l'échange `/api/token` échoue avec un message incompréhensible.

### 3.3 `AuthorizationOutcome`

```kotlin
// core:domain
sealed interface AuthorizationOutcome {
    data class Code(val value: String) : AuthorizationOutcome
    data object Cancelled : AuthorizationOutcome
    data class Failed(val reason: String) : AuthorizationOutcome
}
```

Aujourd'hui un retour arrière pendant l'autorisation produit `Result.failure(Exception("0"))`
(`0` = `RESULT_CANCELED`) et le snackbar affiche une erreur pour un geste volontaire. Avec un
type scellé, le `when` de l'UI doit traiter l'annulation à part, et le compilateur le vérifie
(`kotlin.md` › *Pattern matching*).

### 3.4 La connexion App Remote exige une `Activity`

**Vérifié le 30/09/2026** : passer l'`applicationContext` à `SpotifyAppRemote.connect` ne
fonctionne pas, il faut une `Activity`. Le code actuel passe `showAuthView = false` au
`connect` ; le message d'erreur exact n'a pas été relevé.

Aujourd'hui l'`Activity` arrive par un chemin détourné : `AppRoot` appelle
`requestAuthorization(launcher, it, it, …)` — la même Activity en `activity` et en `context` — et
`SpotifySessionManager.connectRemote(context, …)` la transmet à `SpotifyAppRemoteProvider`. Une
fois ce paramètre sorti du contrat pur, un composant Android doit fournir l'Activity sans
qu'elle réapparaisse dans `core:domain`.

| Option | Principe | Pour | Contre |
|---|---|---|---|
| **1. Liaison explicite depuis `MainActivity`** | `spotify-lib` expose un `DefaultLifecycleObserver` (`AppRemoteLifecycleBinder`) ; `MainActivity` fait `lifecycle.addObserver(binder)`. Connexion avec l'Activity sur `ON_START`, déconnexion sur `ON_STOP` | Explicite, sans état global, testable. `MainActivity` est la racine de composition, comme `app/di/` | Une ligne de câblage dans `MainActivity` |
| **2. Fournisseur de l'Activity au premier plan** | `Application.ActivityLifecycleCallbacks` garde une `WeakReference` vers l'Activity au premier plan ; `SpotifySessionManager` la lit dans `resumeSession()` | Contrat pur inchangé, rien à câbler côté UI | Dépendance cachée à un état global ; rien à lire sans Activity au premier plan ; fuite si la `WeakReference` est oubliée |
| **3. L'Activity dans le contrat** | `resumeSession(host: Activity)` | Rien à inventer | Annule le chantier : `SessionManager` ne monte plus dans `core:domain` |

**Recommandation : option 1** (décision ouverte).

- Le SDK exige une Activity : c'est l'Activity qui porte la connexion, et la symétrie
  `ON_START`/`ON_STOP` est acquise par construction — `AppRoot` n'a aujourd'hui que la moitié
  `ON_STOP`.
- Le contrat pur se réduit : `resumeSession()` et `shutDown()` ne sont plus appelés par l'UI mais
  par le binder. Il reste `sessionState`, `completeAuthorization` et `signOut` côté port ;
  l'implémentation reçoit du binder « Activity disponible / Activity partie ».
- Les ViewModels restent purs. Le seul nouvel import de `spotify-lib` est dans `MainActivity` —
  à tolérer explicitement dans `appDependsOnSpotifyLibInternals` comme racine de composition.
- `LifecycleObserverComponent` et son `onStopCallback` quittent `AppRoot`.

`ON_STOP` arrive aussi à la rotation (l'Activity est recréée) : l'option 1 déconnecte et
reconnecte à chaque rotation, comme le code actuel. Si c'est gênant, ignorer l'arrêt quand
`activity.isChangingConfigurations` est vrai.

---

## 4. Où vit le contrat

La contrainte vient d'Android : **un `ActivityResultLauncher` doit être enregistré avant que
l'Activity soit STARTED**. Il appartient à l'UI et n'entre jamais dans un ViewModel. Le
*contrat*, lui, est sans état : il peut vivre n'importe où.

| Option | Principe | Pour | Contre |
|---|---|---|---|
| **A** | L'écran de connexion importe `SpotifyAuthorizationContract` | Simple | Un import de `spotify-lib` reste dans le code d'un écran |
| **B** | `spotify-lib` fournit via Hilt un `ActivityResultContract<AuthorizationOptions, AuthorizationOutcome>`, reçu sous son type Android générique | Aucun import de `spotify-lib` hors `app/di/` | Une indirection Hilt de plus |

Dans les deux cas, l'arête Gradle `app → spotify-lib` reste : `app` est la racine de
composition, et Hilt doit y voir les `@InstallIn` de la librairie. Ce qui change, ce sont les
**imports dans le code** — c'est ce que compte `appDependsOnSpotifyLibInternals`.

### Configuration dans l'option B

`AuthConfigK` mélange aujourd'hui deux notions :

- **l'identité de l'app** (`clientId`, `redirectUri`, `scopes`), fixe pour l'installation →
  `SpotifyAuthConfig`, public dans `spotify-lib`. La librairie déclare `@BindsOptionalOf` : `app`
  peut la fournir, sinon `Optional.orElse(DEFAULT_AUTH_CONFIG)`. `List` au lieu d'`Array` : une
  `data class` compare un `Array` par référence, d'où l'`equals` écrit à la main aujourd'hui ;
- **l'option d'un lancement** (`forceApprovalDialog`), variable d'un appel à l'autre →
  `AuthorizationOptions`, dans `core:domain`, passée à `launcher.launch(options)`.

Surcharger `redirectUri` impose aussi de modifier l'intent-filter du manifest.

---

## 5. Étapes, test-first

| # | Étape | Dette attendue |
|---|---|---|
| 1 | Tests de la machine à états sur le contrat pur (fakes, Turbine, `runTest`) | — |
| 2 | `SpotifyAuthorizationContract` + `AuthorizationOutcome`, suppression d'`AuthClient` | `spotifyLibDomainImpure` −1, `contractsToMakeInternal` −1 |
| 3 | `SessionManager` purifié et monté dans `core:domain` | `contractsToMoveToCoreDomain` 1 → 0, `spotifyLibDomainImpure` −1 |
| 4 | UI adaptée : `RememberSessionManager`, `SpotifySessionEntryPoint`, écran de connexion, liaison de l'App Remote au cycle de vie de l'Activity (§ 3.4) | `viewModelsDependingOnSpotifyLib` et `appDependsOnSpotifyLibInternals` diminuent |
| 5 | `FakeSessionManager` dans `fake` | `@Preview` de l'écran de connexion dans tous ses états |

Le cliquet de `ArchitectureDebt.kt` ne fait que rétrécir : une nouvelle violation fait échouer
la règle, une entrée corrigée mais encore listée fait échouer `ArchitectureDebtTest`. Retirer
l'entrée dans le commit du correctif.

### À vérifier avant de commencer

- ~~`SpotifyAppRemote.connect` accepte-t-il un `@ApplicationContext` plutôt qu'une `Activity` ?~~
  **Non**, vérifié le 30/09/2026 en lui passant l'`applicationContext` — voir § 3.4.
- `app` passe-t-il aujourd'hui un `AuthConfigK` non `null` ?
- La définition de `appDependsOnSpotifyLibInternals` dans `konsist/` : l'option B la fait-elle
  tomber à zéro ?

---

## 6. Principes en jeu

- **Couche anti-corruption** : le modèle Android + Spotify est traduit à la frontière
  (`SpotifyAuthorizationContract`) et ne fuit pas vers le domaine.
- **Inversion de dépendances** (le D de SOLID) : les ViewModels dépendent d'un port pur de
  `core:domain` ; `spotify-lib` en fournit l'adaptateur (`contracts-cleanup.md`, ports et
  adapters).
- **Flux piloté par l'état** : le contrat publie ce dont il a besoin
  (`AuthorizationRequired`) plutôt que d'imposer un ordre d'appels.
- **Acquisition symétrique au cycle de vie** : ce qui est déconnecté sur `ON_STOP` est
  reconnecté sur `ON_START`, par le même composant (§ 3.4).

Parallèle iOS : envelopper `ASWebAuthenticationSession` derrière un protocole qui renvoie un
`enum AuthOutcome`, sans jamais exposer de `UIViewController` au domaine.
