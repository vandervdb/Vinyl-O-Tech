package org.vander.android.vinylotech.feature.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PlaylistGrid(
        playlists = state.playlists.items,
        playingPlaylistId = state.playingPlaylistId,
        onPlaylistClick = { playlist -> viewModel.playPlaylist(playlist.id) },
        modifier = Modifier.fillMaxSize(),
    )
}
