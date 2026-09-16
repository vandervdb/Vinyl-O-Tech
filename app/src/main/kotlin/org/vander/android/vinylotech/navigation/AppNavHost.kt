package org.vander.android.vinylotech.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.navigation
import org.vander.android.vinylotech.feature.connection.ConnectionRoute
import org.vander.android.vinylotech.feature.connection.connectionNavGraph
import org.vander.android.vinylotech.feature.home.HomeRoute
import org.vander.android.vinylotech.feature.home.homeNavGraph
import org.vander.android.vinylotech.feature.library.libraryNavGraph
import org.vander.core.logger.Logger

/**
 * Holds the graph. The destinations themselves live in the `NavGraphBuilder` extensions
 * ([connectionNavGraph], [homeNavGraph], [libraryNavGraph]), so this function only wires
 * the controller, the start destination and the content inset.
 *
 * `startDestination` is `ConnectionRoute::class` — the KClass overload is the one that
 * takes an `@Serializable object` route. A bottom-bar tab would compile there too but would
 * say the wrong thing: a tab is not the app's entry point.
 *
 * The tabs sit inside [MainGraph] rather than at the root. Nesting buys two things that
 * a flat graph cannot express: a single `popUpTo` target for the whole post-login stack,
 * and a `NavBackStackEntry` that survives tab changes, which is what the MiniPlayer's
 * ViewModel is scoped to.
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    contentPadding: PaddingValues,
    onContinueWithSpotify: () -> Unit,
    logger: Logger,
) {
    NavHost(
        navController = navController,
        startDestination = ConnectionRoute::class,
        modifier = Modifier.padding(contentPadding),
    ) {
        connectionNavGraph(
            onContinueWithSpotify,
            // Navigating to a graph lands on its start destination, so this stays correct
            // if Accueil ever stops being the first tab. `inclusive = true` drops
            // ConnectionRoute itself: back from Accueil leaves the app rather than
            // returning to a login screen the session has already passed.
            onSessionReady = {
                navController.navigate(MainGraph) {
                    popUpTo(ConnectionRoute) { inclusive = true }
                    launchSingleTop = true
                }
            },
        )

        navigation<MainGraph>(startDestination = HomeRoute::class) {
            homeNavGraph()
            libraryNavGraph(navController, logger)
        }
    }
}
