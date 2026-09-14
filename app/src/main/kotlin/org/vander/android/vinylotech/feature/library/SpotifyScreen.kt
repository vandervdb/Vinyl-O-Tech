package org.vander.android.vinylotech.feature.library

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import org.vander.core.logger.Logger
import org.vander.core.ui.presentation.viewmodel.PlayerViewModel
import org.vander.core.ui.presentation.viewmodel.PlaylistViewModel
import org.vander.core.ui.presentation.viewmodel.UserViewModel

@Composable
fun SpotifyScreen(
    playerViewModel: PlayerViewModel,
    playlistViewModel: PlaylistViewModel,
    userViewModel: UserViewModel,
    logger: Logger,
) {
    val tag = "SpotifyScreen"

    Text("Spotify Screen")
}
