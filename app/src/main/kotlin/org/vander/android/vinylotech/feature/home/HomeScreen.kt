package org.vander.android.vinylotech.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.vander.core.logger.Logger

@Composable
fun HomeScreen(
    viewmodel: HomeViewModel,
    logger: Logger,
) {
    val tag = "HomeScreen"

    val playlists by viewmodel.playlists.collectAsStateWithLifecycle()
    logger.d(tag, "playlists: $playlists")

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        PlaylistGrid(
            playlists = playlists.items,
            playingPlaylistId = null,
            onPlaylistClick = { playlist -> viewmodel.playPlaylist(playlist.id) },
        )
    }
}
