package org.vander.android.vinylotech.feature.home

import kotlinx.coroutines.flow.StateFlow
import org.vander.core.domain.data.PlaylistCollection

interface HomeViewModel {
    val playlists: StateFlow<PlaylistCollection>

    fun playPlaylist(playlistId: String)
}
