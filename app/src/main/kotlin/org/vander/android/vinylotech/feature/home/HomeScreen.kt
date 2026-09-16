package org.vander.android.vinylotech.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
) {
    val tag = "HomeScreen"

    val playlists by viewModel.playlists.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        PlaylistGrid(
            playlists = playlists.items,
            playingPlaylistId = null,
            onPlaylistClick = { playlist -> viewModel.playPlaylist(playlist.id) },
        )
    }
}
