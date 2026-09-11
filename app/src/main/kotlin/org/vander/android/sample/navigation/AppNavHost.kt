package org.vander.android.sample.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import org.vander.android.sample.feature.connection.ConnectionRoute
import org.vander.android.sample.feature.connection.connectionNavGraph
import org.vander.android.sample.feature.home.homeNavGraph
import org.vander.android.sample.feature.library.libraryNavGraph
import org.vander.core.logger.Logger

/**
 * Holds the graph. The destinations themselves live in the `NavGraphBuilder`
 * extensions ([connectionNavGraph], [appNavGraph]), so this function only wires
 * the controller, the start destination and the content inset.
 *
 * `startDestination` is `ConnectionRoute::class` — the KClass overload is the one
 * that takes an `@Serializable object` route. Passing `NavItem.Home.route` would
 * also compile (it is a KClass too) but would say the wrong thing: a bottom-bar
 * tab is not the app's entry point.
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
        connectionNavGraph(onContinueWithSpotify)
        homeNavGraph()
        libraryNavGraph(logger)
    }
}
