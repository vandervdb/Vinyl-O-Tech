# Refactors évoqués le 28/09/2026

Branche `refactor/contracts-cleanup`. Point de départ : `SpotifyRemoteAuthDataSource`, puis
toute la chaîne du token et de la session. Chaque entrée dit **quoi**, **pourquoi**, et son
**état** : fait, à faire, ou à décider.

---

## 1. Fait

### 1.1 `SpotifyRemoteAuthDataSource` — échange et refresh factorisés

- `fetchAccessToken` et `refreshAccessToken` passent par un seul `requestToken(action, form)` :
  header `Basic base64(id:secret)`, statut vérifié par `isSuccess()`, erreur de l'accounts
  service lue par `describeError` (enveloppe plate `{"error", "error_description"}`, que
  `parseSpotifyResult` ne sait pas lire).
- Bugs corrigés du refresh : `Authorization: application/x-www-form-urlencoded` (confusion avec
  `Content-Type`), `client-id` au lieu de `client_id` (et inutile hors PKCE), statut ignoré,
  `if` vide, variable qui masque le paramètre.
- `CancellationException` relancée avant le `catch (Exception)`, pour ne pas avaler l'annulation
  d'une coroutine.
- **Tests** : `spotify-lib/src/test/.../datasource/RemoteAuthDataSourceTest.kt`, 5 cas. Rouge
  vérifié sur l'ancien code (3 échecs pour la bonne raison), vert sur le nouveau. L'assertion
  du header Basic utilise `assertTrue` et pas `assertEquals`, pour ne pas imprimer le secret
  encodé dans un rapport de test.

### 1.2 Cycle de dépendances Hilt cassé

`TokenProvider → AuthRepository → RemoteAuthDataSource → @Named("AuthHttpClient") → TokenProvider`.
Le client d'auth recevait un `TokenProvider` dont il ne se sert jamais
(`enableAuthPlugin = false`). Il ne le reçoit plus. `checkNotNull` quand le plugin Auth est
activé sans provider.

### 1.3 Logs du client d'auth

`LogLevel.ALL` → `INFO`, aligné sur les autres clients : `HEADERS` et `BODY` imprimaient le
header Basic (le secret) et les tokens émis dans logcat.

### 1.4 `NetworkModule` → `network/KtorClientFactory.kt`

`provideKtorClient` était annotée `@Provides @Singleton` mais n'était appelée que comme une
fonction ordinaire. La liaison `HttpClient` sans qualifier n'était injectée nulle part, et elle
était insatisfiable (elle demandait un `KtorClientConfig` sans qualifier, qui n'existe pas).
Dagger ne le voyait pas, parce qu'il ne valide que les liaisons atteignables. Le `@Singleton`
n'avait aucun effet sur des appels directs.

Maintenant : fonction de haut niveau `createKtorClient(config, outputLogger, tokenProvider = null)`,
sans annotation Hilt. L'unicité de chaque client vient du `@Singleton` du `@Provides` `@Named`
qui l'appelle.

### 1.5 `SessionManager` : `domain/data/session/` → `domain/session/`

`domain/data` mélangeait deux couches et ne contenait que ce fichier. Le chemin était hérité du
monorepo (commit initial `358824d`), sans raison documentée. 8 imports mis à jour, et les deux
entrées de `ArchitectureDebt.kt` (`spotifyLibDomainImpure`, `contractsToMoveToCoreDomain`)
pointent sur le nouveau chemin. Déplacement intermédiaire : le chantier 3 fera de toute façon
passer le contrat dans `core:domain`.

---

## 2. À faire — la chaîne de refresh du token

### 2.1 `AuthRepository.fetchRefreshedTokenResponse(refreshToken)`

La brique qui manque entre `RemoteAuthDataSource.refreshAccessToken` et le provider. La réponse
du refresh n'a généralement pas de `refresh_token` : `storeTokenResponse` retombe déjà sur celui
qui est stocké.

### 2.2 `SpotifyTokenProvider.refresh(staleAccessToken)` — `Mutex` + vérification

- **Pourquoi un `Mutex`** : Ktor regroupe les 401 concurrents *d'un même client*
  (`AuthTokenHolder.refreshTokensDeferred`, vérifié dans le bytecode 2.3.12). Il ne protège ni
  contre un second appelant hors Ktor (refresh proactif au démarrage), ni contre un check-then-act
  concurrent. Si Spotify fait tourner le refresh token, le perdant finit en `invalid_grant`, et
  l'utilisateur est déconnecté.
- **Pourquoi `staleAccessToken`** : c'est la copie qui a pris le 401 (`oldTokens` côté Ktor). Si
  le stockage contient déjà un autre token, quelqu'un a rafraîchi entre-temps : on relit le
  stockage, sans appel réseau. Ça couvre aussi le 401 *tardif* (requête partie avec l'ancien
  token après un refresh terminé), que le `Deferred` de Ktor ne couvre pas.
- `kotlinx.coroutines.sync.Mutex`, pas `synchronized` (le bloc suspend). Non réentrant : ne pas
  rappeler une méthode qui reprend le verrou.
- À trancher : `withContext(NonCancellable)` autour de `storeTokenResponse`, pour ne pas perdre un
  refresh token tourné si la coroutine est annulée entre la réponse réseau et l'écriture.
- **Tests d'abord** : deux `refresh("A")` concurrents, donc un seul appel réseau. Un
  `refresh("A")` avec B déjà stocké ne fait aucun appel réseau.
- État : interface et implémentation écrites par Arnaud. `fetchRefreshedTokenResponse` et les
  tests restent à faire.

### 2.3 `refreshTokens` de Ktor

```kotlin
tokenProvider
    .refresh(staleAccessToken = oldTokens?.accessToken)
    .map { tokenProvider.currentToken() }
    .getOrNull()
```

Corrige un bug : avant, un refresh en échec renvoyait l'ancien token, et Ktor rejouait la requête
avec le token refusé. Maintenant, `null` fait remonter le 401 d'origine. État : écrit.

### 2.4 `SpotifySessionManager` au démarrage

Le bloc `requestAuthorization` à reprendre :
- le `Result` de `fetchRefreshedTokenResponse` était jeté : rien n'était stocké, `connectRemote`
  n'était jamais appelé, et la session restait bloquée en `Authorizing` ;
- passer par `TokenProvider.refresh(staleAccessToken = tokens.accessToken)` et pas par
  `AuthRepository`, pour ne pas contourner le `Mutex` ;
- guard clauses avec `getOrElse { …; return@launch }` au lieu d'`if` imbriqués ;
- log « Connecting to remote » émis au mauvais endroit ;
- injecter un `java.time.Clock` au lieu de `System.currentTimeMillis()`, pour tester la branche
  « expiré ».

**À décider** : App Remote n'utilise pas le token Web API, et Ktor rafraîchit déjà au premier
401. Le refresh proactif ne sert qu'à détecter au démarrage un refresh token révoqué. Sinon, on
supprime la branche, et `SessionTokens.isAccessTokenExpired` n'a plus d'utilisateur.

---

## 3. Chantier — couche anti-corruption de la session

Objectif : sortir `Activity`, `Context`, `ActivityResultLauncher` et `ActivityResult` des
contrats, pour que `SessionManager` monte dans `core:domain` et que les ViewModels ne dépendent
plus de `spotify-lib`. C'est le premier point de *Ensuite* dans `contracts-cleanup.md`.

### 3.1 Contrat pur

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

- Nouvel état `SessionState.AuthorizationRequired`. Le flux est piloté par l'état, et plus par
  un ordre d'appels imposé.
- `Context` : `@ApplicationContext` injecté (vérifier que `SpotifyAppRemote.connect` l'accepte).
- `CoroutineScope`/`dispatcher` en paramètres : remplacés par le `@ApplicationScope` existant.

### 3.2 `SpotifyAuthorizationContract : ActivityResultContract<I, AuthorizationOutcome>`

Remplace `AuthClient` et `SpotifyAuthClient`, dont les deux méthodes sont exactement
`createIntent` et `parseResult`.

- `createIntent` : `context as? Activity ?: error(...)`, parce que
  `createLoginActivityIntent(Activity, …)` exige une `Activity`. `ComponentActivity` passe
  elle-même.
- Couture testable : fonction pure `toOutcome(type, code, error)`, qui remplace la sous-classe
  `ParsedAuth`/`parseAuthResponse` (`AuthorizationResponse.Builder` n'est pas utilisable dans un
  test).
- Bug corrigé au passage : un `TOKEN` était renvoyé comme s'il s'agissait d'un code.

### 3.3 `AuthorizationOutcome` (sealed, `core:domain`)

`Code(value)`, `Cancelled`, `Failed(reason)`. L'annulation n'est plus une erreur : aujourd'hui,
`RESULT_CANCELED` donne `Failure(Exception("0"))`.

### 3.4 Où va le contrat — options

- **A** : `app` (l'écran de connexion) importe `SpotifyAuthorizationContract`, une API publique
  d'entrée. Plus simple.
- **B** : `spotify-lib` fournit via Hilt un
  `ActivityResultContract<AuthorizationOptions, AuthorizationOutcome>`, qu'`app` reçoit injecté
  (un contrat est sans état, il peut vivre dans un ViewModel, contrairement au launcher). Aucun
  import de `spotify-lib` hors `app/di/`.
- `app` garde de toute façon `implementation(project(":spotify-lib"))` : c'est la racine de
  composition, et Hilt doit voir les `@InstallIn` de `spotify-lib`. Ce qui change, ce sont les
  imports dans le code, pas l'arête Gradle.
- À vérifier : la définition de `appDependsOnSpotifyLibInternals` dans `konsist/`.

### 3.5 Config dans l'option B

- **Identité de l'app** (`clientId`, `redirectUri`, `scopes`) : `SpotifyAuthConfig` (public, dans
  `spotify-lib`, `List` au lieu d'`Array`, ce qui supprime l'`equals` écrit à la main). La
  librairie déclare `@BindsOptionalOf`, et `app/di` peut la fournir. Sinon, on applique
  `Optional.orElse(DEFAULT_AUTH_CONFIG)`.
- **Option d'un lancement** : `AuthorizationOptions(forceApprovalDialog)` dans `core:domain`,
  passée en `input` de `launch(...)`.
- Surcharger `redirectUri` impose aussi de modifier l'intent-filter du manifest.
- À vérifier : `app` passe-t-il un `AuthConfigK` non `null` aujourd'hui ?

### 3.6 Étapes et compteurs de dette attendus

1. Tests de la machine à états sur le contrat pur (fakes, Turbine, `runTest`)
2. `SpotifyAuthorizationContract` + `AuthorizationOutcome`, et suppression d'`AuthClient` :
   `spotifyLibDomainImpure` −1, `contractsToMakeInternal` −1
3. `SessionManager` purifié et monté dans `core:domain` : `contractsToMoveToCoreDomain` 1 → 0,
   `spotifyLibDomainImpure` −1
4. UI adaptée (`RememberSessionManager`, `SpotifySessionEntryPoint`, écran de connexion) :
   `viewModelsDependingOnSpotifyLib` et `appDependsOnSpotifyLibInternals` diminuent
5. `FakeSessionManager` dans `fake`, pour les `@Preview`

---

## 4. Petits correctifs identifiés

- **`sendWithoutRequest`** (`KtorClientFactory.kt`) : `request.url.host == HTTPS_API_SPOTIFY_COM_V_1`
  compare un host à une URL complète, donc la condition n'est jamais vraie. Chaque session
  commence par une requête sans `Authorization` et un 401. Correctif probable :
  `Url(HTTPS_API_SPOTIFY_COM_V_1).host`. Test rouge d'abord.
- **KDoc à reprendre** (proposés, non appliqués) :
  - `AuthRepository.getTokens` : « empty string » est faux, c'est `null` ;
  - `TokenProvider` : lien vers `core.domain.auth.AuthRepository` cassé, `[tokenFlow]`
    inexistant, et « must never write » contredit `refresh()` ;
  - `SpotifyRemoteAuthDataSource` : l'en-tête ne parle que de l'échange de code, et
    l'avertissement sur les logs est obsolète ;
  - `RemoteAuthDataSource` : `refreshAccessToken` n'est pas documenté, et « non-200 » doit
    devenir « non-2xx » ;
  - `SpotifyAuthRepository` : un historique au lieu d'un contrat ;
  - `SessionTokens` : unité de `expiresAt` (epoch ms) et rôle de `margin`.
- `SpotifyRemoteAuthDataSource.httpClient` : `val` public, à passer en `private`.

---

## 5. Dette et points ouverts signalés

- **Arborescence `spotify-lib/.../di/` supprimée** (25 suppressions indexées, plus aucun
  `@Module` dans `spotify-lib`) en cours de session. Si c'est un déplacement, reporter les
  changements 1.2, 1.3 et 1.4 dans `RemoteAuthModule` et `KtorClientConfigModule`.
- **Konsist en échec** : `Layer spotify-lib di doesn't contain any files`, et entrées de dette
  périmées après les renommages (`IAuthRepository`, `ITokenProvider`, `IDataStoreManager`,
  `DataStoreTokenProvider`, `data.repository.AuthRepository`, `SpotifySessionManagerImpl`).
- **`public_api_v1_client` / `public_api_v1`** : aucun consommateur. Liaison morte, à supprimer
  ou à justifier.
- **Secret client** : le Base64 de `CLIENT_ID:CLIENT_SECRET` est apparu dans une sortie de test
  pendant la session. Régénérer le secret dans le dashboard Spotify si ce client compte.
- **Docs qui citent `NetworkModule`** : `spotify-lib/README.md` (l. 13, 197, 200),
  `CLAUDE.md` (l. 65), `.claude/rules/kotlin.md` (l. 63).
- **Tests encore sur l'ancienne API** : `AuthHeaderPluginTest` et `RemotePlaylistDataSourceTest`
  implémentent `ITokenProvider`, `tokenFlow` et `getAccessToken()`.
- `core/security/.../SecureTokenStorage.kt` (l. 7-11) cite `DataStoreManager`/`IDataStoreManager`,
  qui ont été supprimés.
- `docs/architecture/contracts-cleanup.md` : compteurs du cliquet à mettre à jour après les
  renommages.
