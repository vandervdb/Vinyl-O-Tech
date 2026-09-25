package org.vander.android.vinylotech.feature.library

import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.vander.android.vinylotech.feature.player.SpotifyPlayerViewModel
import org.vander.android.vinylotech.navigation.MainGraph
import org.vander.core.logger.Logger

@Serializable
object SpotifyRoute

fun NavGraphBuilder.libraryNavGraph(
    navController: NavController,
    logger: Logger,
) {
    composable<SpotifyRoute> { entry ->
        // Scoped to MainGraph, like the MiniPlayer's in AppRoot: both resolve the same
        // instance. Scoped to this destination, it was a second SpotifyPlayerViewModel, cleared on
        // every tab change.
        val graphEntry = remember(entry) { navController.getBackStackEntry<MainGraph>() }
        val playerViewModel = hiltViewModel<SpotifyPlayerViewModel>(graphEntry)
        val playlistViewModel = hiltViewModel<SpotifyPlaylistViewModel>()
        val userViewModel = hiltViewModel<SpotifyUserViewModel>()

        SpotifyScreen(
            playerViewModel = playerViewModel,
            playlistViewModel = playlistViewModel,
            userViewModel = userViewModel,
            logger = logger,
        )
    }
}
