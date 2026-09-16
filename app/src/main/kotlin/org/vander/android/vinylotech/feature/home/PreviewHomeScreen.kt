package org.vander.android.vinylotech.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.vander.android.vinylotech.designsystem.AndroidAppTheme
import org.vander.core.domain.data.Playlist
import org.vander.core.domain.data.PlaylistCollection

/**
 * Tapping a tile marks it as playing, so the disc animation can be tried on the canvas. What the
 * interface buys: a preview without Hilt, a network or an App Remote.
 */
private class PreviewHomeViewModel : HomeViewModel {
    private val _state =
        MutableStateFlow(
            HomeUiState(
                playlists =
                    PlaylistCollection(
                        listOf("Sillons", "Marée haute", "Braise", "Gris perle", "Or mat", "Onde longue")
                            .mapIndexed { index, name -> Playlist(id = "p$index", name = name, coverUrl = "") },
                    ),
                playingPlaylistId = "p2",
            ),
        )
    override val state: StateFlow<HomeUiState> = _state

    override fun playPlaylist(playlistId: String) {
        _state.update { it.copy(playingPlaylistId = playlistId) }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0A10)
@Composable
private fun HomeScreenPreview() {
    AndroidAppTheme {
        HomeScreen(viewModel = remember { PreviewHomeViewModel() })
    }
}
