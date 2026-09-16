package org.vander.android.vinylotech.feature.home

import kotlinx.coroutines.flow.StateFlow
import org.vander.core.domain.data.PlaylistCollection

/**
 * What the Accueil screen draws.
 *
 * @property playingPlaylistId the playlist playback is running from; `null` while nothing
 *   plays, or while an album or an artist does.
 */
data class HomeUiState(
    val playlists: PlaylistCollection = PlaylistCollection.empty(),
    val playingPlaylistId: String? = null,
)

/**
 * Contract of the Accueil screen. Kept in `feature/home` rather than `core:ui`: no other module
 * implements it, and a preview can implement it locally.
 */
interface HomeViewModel {
    val state: StateFlow<HomeUiState>

    fun playPlaylist(playlistId: String)
}
