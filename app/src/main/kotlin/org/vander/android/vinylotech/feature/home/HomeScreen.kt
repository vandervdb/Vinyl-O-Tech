package org.vander.android.vinylotech.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeContent(
        state,
        onPlaylistClick = { playlist -> viewModel.playPlaylist(playlist.id) },
    )
}
