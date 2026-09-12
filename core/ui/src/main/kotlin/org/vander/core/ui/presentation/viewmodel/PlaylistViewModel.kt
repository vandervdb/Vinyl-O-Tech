package org.vander.core.ui.presentation.viewmodel

import kotlinx.coroutines.flow.StateFlow
import org.vander.core.domain.data.PlaylistCollection

/**
 * Contract for the playlist grid, implemented by the app's ViewModel and by
 * [org.vander.fake.spotify.FakePlaylistViewModel].
 *
 * [refresh] reloads the collection from the Web API and publishes the result on [playlists].
 */
interface PlaylistViewModel {
    val playlists: StateFlow<PlaylistCollection>

    /**
     * Reloads the playlists and publishes them on [playlists]. Returns immediately; a failure
     * is logged and surfaces as an empty collection, so the caller cannot tell the two apart.
     */
    fun refresh()
}
