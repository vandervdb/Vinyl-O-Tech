package org.vander.android.vinylotech.feature.home

import androidx.annotation.StringRes
import kotlinx.coroutines.flow.StateFlow
import org.vander.android.vinylotech.R
import org.vander.core.domain.data.PlaylistCollection
import org.vander.core.domain.data.RecentPlay

/**
 * What the Accueil screen draws.
 *
 * Every field has a default, so a ViewModel can fill them one at a time: a field it does not
 * provide yet simply renders its empty state.
 *
 * @property playingPlaylistId the playlist playback is running from; `null` while nothing
 *   plays, or while an album or an artist does.
 * @property resume what the hero puts forward; `null` hides the block and leaves the backdrop.
 * @property selectedFilter the chip currently selected.
 */
data class HomeUiState(
    val playlists: PlaylistCollection = PlaylistCollection.empty(),
    val playingPlaylistId: String? = null,
    val resume: ResumeListening? = null,
    val selectedFilter: HomeFilter = HomeFilter.All,
    val recentlyPlayed: List<RecentPlay> = emptyList(),
)

/**
 * The item the hero invites to resume, already shaped as text: the screen does not decide how
 * the subtitle is composed.
 *
 * @property subtitle the context line, e.g. "Elia Faure · 2023 · 11 titres".
 * @property isSaved `null` hides the heart — unknown, or not something that can be saved.
 */
data class ResumeListening(
    val title: String,
    val subtitle: String,
    val isSaved: Boolean?,
)

/** The chips under the top bar, in the design's order. */
enum class HomeFilter(
    @StringRes val labelRes: Int,
) {
    All(R.string.home_filter_all),
    Albums(R.string.home_filter_albums),
    Playlists(R.string.home_filter_playlists),
    Artists(R.string.home_filter_artists),
}

/**
 * Contract of the Accueil screen. Kept in `feature/home` rather than `core:ui`: no other module
 * implements it, and a preview can implement it locally.
 */
interface HomeViewModel {
    val state: StateFlow<HomeUiState>

    fun playPlaylist(playlistId: String)
}
