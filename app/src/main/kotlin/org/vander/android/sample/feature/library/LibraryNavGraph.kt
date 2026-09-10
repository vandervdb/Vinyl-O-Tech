package org.vander.android.sample.feature.library

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.vander.android.sample.feature.player.PlayerViewModelImpl
import org.vander.core.logger.Logger

@Serializable
object SpotifyRoute

fun NavGraphBuilder.libraryNavGraph(logger: Logger) {
    composable<SpotifyRoute> {
        // Scoped to this NavBackStackEntry, so a fresh instance per visit unless
        // saveState/restoreState kept the entry alive.
        val playerViewModel = hiltViewModel<PlayerViewModelImpl>()
        val playlistViewModel = hiltViewModel<PlayListViewModelImpl>()
        val userViewModel = hiltViewModel<UserViewModelImpl>()

        SpotifyScreen(
            playerViewModel = playerViewModel,
            playlistViewModel = playlistViewModel,
            userViewModel = userViewModel,
            launchStartup = true,
            logger = logger,
        )
    }
}
