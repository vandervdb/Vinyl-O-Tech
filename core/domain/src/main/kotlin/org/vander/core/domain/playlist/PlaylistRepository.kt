package org.vander.core.domain.playlist

import kotlinx.coroutines.flow.StateFlow
import org.vander.core.domain.data.PlaylistCollection

/**
 * The user's playlists.
 *
 * [refresh] returns the outcome *and* publishes the playlists on [playlists]: a caller that
 * needs to react to the failure uses the [Result], one that only displays the data collects
 * the flow. The flow is never `null`: before the first [refresh] it holds an empty collection,
 * which is therefore indistinguishable from an empty library.
 */
interface PlaylistRepository {
    val playlists: StateFlow<PlaylistCollection>

    suspend fun refresh(): Result<Unit>
}
