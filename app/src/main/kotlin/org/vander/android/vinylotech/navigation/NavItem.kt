package org.vander.android.vinylotech.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph
import org.vander.android.vinylotech.R
import org.vander.android.vinylotech.feature.home.HomeRoute
import org.vander.android.vinylotech.feature.library.SpotifyRoute

/**
 * A tab of the dock.
 *
 * @property route the `@Serializable object` route itself, not its class: the instance is what
 *   `navigate` takes, and its class is what `hasRoute` compares.
 */
sealed class NavItem(
    val route: Any,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    /**
     * Whether this tab is the one [destination] belongs to.
     *
     * Walks the hierarchy rather than comparing the destination itself, so a screen pushed from
     * a tab later on still lights up that tab.
     */
    fun isSelectedIn(destination: NavDestination?): Boolean =
        destination?.hierarchy?.any { it.hasRoute(route::class) } == true

    companion object {
        val all = listOf(Home, Spotify)
    }

    object Home : NavItem(HomeRoute, R.string.nav_label_home, Icons.Filled.Home, Icons.Outlined.Home)

    object Spotify :
        NavItem(
            SpotifyRoute,
            R.string.nav_label_spotify,
            Icons.Filled.LibraryMusic,
            Icons.Outlined.LibraryMusic,
        )
}

/**
 * Switches to [item]'s tab the way a bottom navigation should.
 *
 * - `popUpTo` the first tab of [MainGraph], with `saveState`: tabs do not pile up on the back
 *   stack, back from any tab returns to the first one, and each tab keeps its own stack.
 * - `launchSingleTop`: tapping the current tab does not push it a second time.
 * - `restoreState`: coming back to a tab restores where it was left.
 *
 * The first tab is read from the graph rather than named, so this stays right if the tab order
 * changes. Only call it while [MainGraph] is on the back stack — which is always the case when
 * the dock is visible, since only [MainGraph] destinations carry it.
 */
fun NavController.navigateToTab(item: NavItem) {
    val tabs = getBackStackEntry<MainGraph>().destination as NavGraph
    navigate(item.route) {
        popUpTo(tabs.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
