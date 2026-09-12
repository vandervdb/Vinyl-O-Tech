package org.vander.spotifyclient.domain.repository

import kotlinx.coroutines.flow.StateFlow
import org.vander.core.domain.data.CurrentlyPlaying

/**
 * The playback queue as the Web API sees it.
 *
 * Same shape as [SpotifyPlaylistRepository]: [getUserQueue] both returns and publishes, and
 * `null` on [currentQueue] means "not loaded yet".
 *
 * The queue is only a snapshot — it does not update on its own when the track changes, so it
 * has to be re-fetched.
 */
interface SpotifyQueueRepository {
    val currentQueue: StateFlow<CurrentlyPlaying?>

    suspend fun getUserQueue(): Result<CurrentlyPlaying>
}
