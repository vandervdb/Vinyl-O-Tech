# Navigation et ScreenChrome

Décisions d'architecture pour la navigation de l'app Android et pour l'encadrement
visuel qui l'accompagne. Tout ce qui est affirmé ici sur la maquette a été relevé
dans le fichier de design, pas supposé — les commandes de relevé sont en annexe.

---

## Contexte : ce que la maquette impose

Le design Vinyl O'Tech décrit 11 écrans. Deux relevés en fixent l'architecture. Ils ont
été refaits le 2026-09-12 sur `Vinyl OTech - App Android V1 (standalone).html`, seul
fichier de design encore présent — celui que cite l'annexe a disparu, et la colonne
MiniPlayer des écrans 05 et 08 y était l'inverse de ce qu'elle est ici.

**Quel écran porte quel encadrement :**

| Écran | Barre de nav | MiniPlayer |
|---|---|---|
| 00 Splash A/B · 01 Connexion | — | — |
| 02 Accueil · 03 Recherche · 04 Bibliothèque · 10 Profil | ✅ | ✅ |
| 05 Album · 08 Artiste | ✅ | ✅ |
| 06 Lecteur · 07 File d'attente · 09 Paroles | — | — |

**Quelle affordance porte chaque écran en haut à gauche :**

| Affordance | Écrans | Ce qu'elle signifie |
|---|---|---|
| aucune | 02 · 03 · 10 | racine d'onglet, rien à quitter |
| `‹` | 05 Album · 08 Artiste | **push** : on empile dans le même contexte |
| `⌄` | 06 Lecteur · 07 File d'attente · 09 Paroles | **modale** : on referme vers le bas |

Les deux tableaux se recouvrent exactement : **tout écran en `⌄` est sans encadrement,
tout écran en `‹` le conserve — entièrement, MiniPlayer compris.** L'affordance n'est pas
un détail graphique, elle encode le registre de navigation.

> L'écran 09 Paroles porte bien un bandeau bas, mais qui lui est propre : pleine largeur,
> `#17112E`, disque de 44 px, sans barre d'onglets ni carte MiniPlayer. Ce n'est pas
> l'encadrement partagé, donc il reste hors de `CHROME`.

> L'écran 04 semble faire exception avec un `⌄`, mais il s'agit du sélecteur de tri
> « Récent ⌄ » à côté du titre, pas d'une affordance de fermeture.

---

## Décision 1 — deux registres de navigation, un seul back stack

L'app a deux registres :

- le **push**, qui empile dans le même contexte visuel : l'encadrement reste, le
  contenu change (Accueil → Album → Artiste)
- la **modale**, qui ouvre un contexte temporaire recouvrant le précédent et se
  referme (MiniPlayer → Lecteur → Paroles)

Ils sont exprimés par la **structure du graphe plus la transition**, pas par des
`NavHost` imbriqués.

**Un seul `NavHost`, donc un seul `NavController`.** C'est la contrainte structurante :
un `NavHost` = un back stack, et Android n'a qu'un bouton retour. Deux hosts
imposeraient d'arbitrer à la main lequel dépiler, feraient traverser les deep links
aux deux, et limiteraient le `popUpTo` de la barre au graphe interne. Coût élevé pour
un gain d'expressivité.

*Alternative écartée* : un `NavHost` externe portant `ConnectionRoute` et une
destination-shell hébergeant un `NavHost` interne pour les onglets. Correspond
littéralement à la maquette, se paie en double back stack.

---

## Décision 2 — l'encadrement est une donnée de la destination

L'encadrement ne dépend **pas** de l'état de lecture. Le MiniPlayer est visible parce
que la maquette le prévoit sur cet écran, pas parce qu'une piste joue. C'est une
donnée connue à la compilation, pas un flux à observer.

```kotlin
data class ScreenChrome(val bottomBar: Boolean, val miniPlayer: Boolean) {
    companion object { val None = ScreenChrome(bottomBar = false, miniPlayer = false) }
}
```

Deux booléens indépendants alors que, dans la maquette actuelle, tout écran qui porte la
barre porte aussi le MiniPlayer : **un seul suffirait aujourd'hui.** Ils restent séparés
parce que ce sont deux décisions de design distinctes qui se trouvent coïncider — une
révision précédente donnait la barre seule à Album et Artiste — et qu'il faudrait défaire
la fusion à la première divergence.

La résolution passe par le **type** de la route, pas par une chaîne :

```kotlin
fun NavDestination?.screenChrome(): ScreenChrome =
    CHROME.entries
        .firstOrNull { (route, _) -> this?.hierarchy?.any { it.hasRoute(route) } == true }
        ?.value ?: ScreenChrome.None
```

Deux points non négociables dans cette fonction :

- **`hierarchy`** et non une comparaison directe : si une route est un jour imbriquée
  dans un sous-graphe, la remontée de hiérarchie continue de matcher.
- **`ScreenChrome.None` par défaut** : oublier une entrée **retire** l'encadrement au
  lieu d'en afficher un faux. L'erreur est visible immédiatement.

Le mot *chrome* désigne, en vocabulaire d'interface, l'encadrement par opposition au
contenu — l'origine est le navigateur, dont la barre d'adresse est le chrome et la page
le contenu. `Scaffold` est d'ailleurs un composant de chrome : ses slots nommés
(`topBar`, `bottomBar`, `floatingActionButton`) sont l'encadrement, son trailing lambda
est le contenu, et `innerPadding` existe parce que l'encadrement occupe de la place.

---

## Décision 3 — le shell entoure le `NavHost` (forme A)

L'encadrement vit **à côté** du contenu, dans un `Box`, et se montre selon la
destination courante.

```kotlin
@Composable
fun MainShell(navController: NavHostController) {
    val backEntry by navController.currentBackStackEntryAsState()
    val chrome = backEntry?.destination.screenChrome()

    var bandHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Box(Modifier.fillMaxSize()) {
        AppNavHost(navController, contentPadding = PaddingValues(bottom = bandHeight))

        if (chrome.bottomBar) {
            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .onSizeChanged { bandHeight = with(density) { it.height.toDp() } }
                    // fond AVANT les insets : il doit couvrir la zone système
                    .background(VinylInk)
                    .navigationBarsPadding()
                    .padding(...),
            ) {
                if (chrome.miniPlayer) MiniPlayer(...)
                AppBottomBar(...)
            }
        }
    }
}
```

**Pourquoi cette forme plutôt qu'un shell répété dans chaque `composable`** : on ouvre
le Lecteur *depuis* le MiniPlayer. Si le MiniPlayer était recréé à chaque destination,
le disque qui tourne et le marquee du titre repartiraient de zéro à chaque fermeture de
modale. Ici il est monté une fois et traverse les transitions.

*Le prix, assumé* : `bandHeight` retombe à `0.dp` sur une destination sans barre puis
remonte au retour — une passe de layout de plus par transition. Invisible parce que la
mesure est consommée en **`contentPadding`** et non en `Modifier.padding` : une lazy
list pose ses items depuis le haut, seule la course de scroll restante change. Ce
choix n'est donc pas cosmétique, il conditionne l'absence de saut visuel.

---

## Décision 4 — pas de retour vers la connexion

```kotlin
navController.navigate(MainGraph) {
    popUpTo(ConnectionRoute) { inclusive = true }
}
```

`inclusive = true` retire **aussi** `ConnectionRoute` de la pile : le bouton retour
depuis Accueil sort de l'app. Sans `inclusive`, on dépile *jusqu'à* la connexion en la
conservant — l'inverse du comportement voulu.

Les `popUpTo` de la barre de navigation utilisent `findStartDestination()` et non
`graph.startDestinationId` : le premier descend jusqu'à la destination feuille, le
second s'arrête au nœud direct, qui peut être un sous-graphe. Sur un graphe plat les
deux coïncident ; ils divergent dès la première imbrication.

---

## Décision 5 — portée des ViewModels

Un `ViewModel` vit exactement aussi longtemps que le `ViewModelStoreOwner` dont il
dépend, et `hiltViewModel()` prend la `NavBackStackEntry` courante — soit « un
ViewModel par écran ».

Le MiniPlayer vit dans le shell, **au-dessus** des destinations. Son `PlayerViewModel`
ne peut donc pas être scopé à une destination, sinon il meurt à chaque changement
d'onglet. Il faut viser l'entrée du sous-graphe :

```kotlin
val parentEntry = remember(backStackEntry) { navController.getBackStackEntry<MainGraph>() }
val playerViewModel = hiltViewModel<PlayerViewModelImpl>(parentEntry)
```

Le corollaire vaut pour l'authentification : `ActivityResultLauncher` et `Activity` ne
peuvent **pas** entrer dans un ViewModel (`architecture.md` : pas de `Context` hors
`@ApplicationContext`). D'où le partage :

| Responsabilité | Où |
|---|---|
| `sessionState: StateFlow<SessionState>` | ViewModel, `SpotifySessionManager` injecté |
| `requestAuthorization(launcher)` · `launchAuthorizationFlow(activity)` · `handleAuthResult` | `AppRoot` |

Le flux reste unidirectionnel : `AppRoot` déclenche → le manager met à jour son
`StateFlow` → le ViewModel le republie → l'écran réagit. Personne ne remonte le courant.

---

## Décision 6 — les écrans reçoivent des intentions

Aucun écran ne reçoit le `NavController` ni un objet de domaine comme
`SpotifySessionManager`. Un écran qui détient le controller peut naviguer n'importe où ;
un écran qui détient le manager de session ne peut pas être prévisualisé, faute de
pouvoir en construire un.

```kotlin
// ❌  fun ConnectionScreen(sessionManager: SpotifySessionManager)
// ✅  fun ConnectionScreen(sessionState: SessionState, onContinueWithSpotify: () -> Unit)
```

Le graphe est le seul endroit qui connaît les routes ; c'est lui qui traduit une
intention en navigation.

---

## Structure résultante

```kotlin
NavHost(navController, startDestination = ConnectionRoute) {

    composable<ConnectionRoute> { ... }          // nu, hors shell

    navigation<MainGraph>(startDestination = HomeRoute) {
        composable<HomeRoute>    { ... }         // onglet
        composable<SearchRoute>  { ... }         // onglet
        composable<LibraryRoute> { ... }         // onglet
        composable<ProfileRoute> { ... }         // onglet
        composable<AlbumRoute>   { ... }         // push, ‹
        composable<ArtistRoute>  { ... }         // push, ‹
    }

    // modales : transition verticale, ⌄ appelle popBackStack()
    composable<PlayerRoute>(enterTransition = { slideInVertically { it } }) { ... }
    composable<QueueRoute>  { ... }
    composable<LyricsRoute> { ... }
}
```

Le sous-graphe `MainGraph` sert deux buts : il donne à `popUpTo` une cible qui dépile
tout le périmètre post-login d'un coup, et il fournit la `NavBackStackEntry` sur
laquelle scoper le `PlayerViewModel` (décision 5). Le shell n'a pas besoin de connaître
la liste des écrans qui le refusent : les modales sont au niveau racine, il ne les
couvre simplement pas.

---

## Ce qu'il faut surveiller

- **La table `CHROME` doit suivre le graphe.** Elle vit à côté de lui dans
  `navigation/`, et le défaut `None` rend l'oubli visible.
- **Une entrée de `CHROME` par route de premier plan seulement.** Une modale n'y figure
  pas ; c'est son absence qui la définit.
- **Ne pas consommer `bandHeight` en `Modifier.padding`.** Voir décision 3.

---

## Annexe — comment les relevés ont été faits

Dans le fichier de design, chaque écran est un `div data-screen-label`. Attention, le
balisage y est échappé (`\u002F`, `\n`, `\"`) : il faut le déséchapper avant toute
recherche, sinon tous les motifs CSS ressortent vides.

Le relevé du 2026-09-12 a été fait sur `Vinyl OTech - App Android V1 (standalone).html`,
en découpant par écran et en testant trois signatures CSS distinctes — le bandeau, la
barre d'onglets et la carte du MiniPlayer, qui ne vont pas systématiquement ensemble :

```python
raw = open("Vinyl OTech - App Android V1 (standalone).html", encoding="utf-8").read()
s = raw.replace("\\u002F", "/").replace("\\n", "\n").replace('\\"', '"')

labels = [(m.start(), m.group(1)) for m in re.finditer(r'data-screen-label="([^"]+)"', s)]
bounds = [(lab, off, labels[i + 1][0] if i + 1 < len(labels) else len(s))
          for i, (off, lab) in enumerate(labels)]

BAND = 'position: absolute; left: 0; right: 0; bottom: 0; z-index: 4'
DOCK = 'justify-content: space-around; background: #16121E'
MINI = 'background: #221C2E; border: 1px solid rgba(124,92,255,0.3); border-radius: 18px'

for lab, a, b in bounds:
    c = s[a:b]
    print(lab, BAND in c, DOCK in c, MINI in c)
```

L'affordance haute se relève de la même façon, en cherchant `‹` et `⌄` dans les
3000 premiers caractères de chaque écran.

Les signatures d'API citées ici (`NavDestination.hasRoute(KClass)`, `getHierarchy`,
les quatre surcharges de `startDestination`, `PopUpToBuilder.inclusive`) ont été
vérifiées dans le bytecode des artefacts effectivement résolus par le build :

```bash
find ~/.gradle/caches/modules-2/files-2.1/androidx.navigation \
     -path '*navigation-common*2.8.4*' -name '*.aar'
unzip -o -q <aar> classes.jar && unzip -o -q classes.jar -d cls
javap -p  cls/androidx/navigation/NavDestination.class
javap -c -p 'cls/androidx/navigation/NavGraph$Companion.class'
```
