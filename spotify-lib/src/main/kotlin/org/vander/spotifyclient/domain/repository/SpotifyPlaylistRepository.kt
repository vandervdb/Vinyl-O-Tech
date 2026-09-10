package org.vander.spotifyclient.domain.repository

import kotlinx.coroutines.flow.StateFlow
import org.vander.core.domain.data.PlaylistCollection

interface SpotifyPlaylistRepository {
    val playlists: StateFlow<PlaylistCollection?>

    suspend fun getUserPlaylists(): Result<PlaylistCollection>
}
