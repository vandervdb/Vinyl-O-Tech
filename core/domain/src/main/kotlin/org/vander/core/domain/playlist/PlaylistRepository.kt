package org.vander.core.domain.playlist
import kotlinx.coroutines.flow.StateFlow
import org.vander.core.domain.data.PlaylistCollection

/**
 * The user's playlists.
 *
 * [getUserPlaylists] returns the outcome *and* publishes it on [playlists]: a caller that
 * needs to react to the failure uses the [Result], one that only displays the data collects
 * the flow. `null` on the flow means "not loaded yet", distinct from an empty collection.
 */
interface PlaylistRepository {
    val playlists: StateFlow<PlaylistCollection>

    suspend fun refresh(): Result<Unit>
}
