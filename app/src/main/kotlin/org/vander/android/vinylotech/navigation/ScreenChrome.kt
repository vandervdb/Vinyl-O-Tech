package org.vander.android.vinylotech.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import org.vander.android.vinylotech.navigation.ScreenChrome.Companion.None
import kotlin.reflect.KClass

/**
 * What a destination carries around its content: the bottom navigation bar and the
 * MiniPlayer. Both are a property of the screen as drawn in the design, not of the
 * playback state — the MiniPlayer shows on Accueil whether or not a track is playing.
 *
 * Two independent flags although, in the current design, every screen carrying the bar
 * carries the MiniPlayer too (02 · 03 · 04 · 05 · 08 · 10) — one Boolean would do today.
 * They stay apart because bar and MiniPlayer are two separate design decisions that
 * happen to agree right now: an earlier revision had Album and Artiste carrying the bar
 * alone, and collapsing them would have to be undone the first time they diverge again.
 */
data class ScreenChrome(
    val bottomBar: Boolean,
    val miniPlayer: Boolean,
) {
    companion object {
        val None = ScreenChrome(bottomBar = false, miniPlayer = false)
    }
}

/**
 * Chrome per route, most specific first.
 *
 * Order matters, and matters for real since the tabs moved inside [MainGraph]:
 * [screenChrome] stops at the first match and [hierarchy] walks up to the parent graph,
 * so every tab matches [MainGraph] too. An exception — a destination inside the graph
 * wanting a different chrome — must therefore be listed *before* it, or the parent entry
 * swallows it with no warning at all.
 *
 * One entry covers every tab, present and future: a new destination added to [MainGraph]
 * inherits the chrome without being registered here.
 *
 * A route missing from this map gets [ScreenChrome.None] — ConnectionRoute is absent on
 * purpose, and so are the modals (Lecteur, File d'attente, Paroles), which live at the
 * root of the graph precisely so that nothing here matches them.
 */
private val CHROME: Map<KClass<*>, ScreenChrome> =
    linkedMapOf(
        // Exceptions go here, above MainGraph.
        MainGraph::class to ScreenChrome(bottomBar = true, miniPlayer = true),
    )

/**
 * Resolves the chrome of the current destination.
 *
 * Two points are not negotiable here:
 * - [hierarchy], not a direct comparison: a route nested in a sub-graph keeps matching
 *   through its parents.
 * - [ScreenChrome.None] as the fallback: a forgotten entry *removes* the chrome instead
 *   of showing a wrong one, which is visible on the first run.
 *
 * The receiver is nullable because `currentBackStackEntryAsState()` is null on the very
 * first composition; that frame renders without chrome, which the start destination
 * (ConnectionRoute) wants anyway.
 */
fun NavDestination?.screenChrome(): ScreenChrome =
    CHROME.entries
        .firstOrNull { (route, _) -> this?.hierarchy?.any { it.hasRoute(route) } == true }
        ?.value ?: None
