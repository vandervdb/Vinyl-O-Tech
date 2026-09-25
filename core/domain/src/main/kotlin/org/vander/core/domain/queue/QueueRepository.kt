package org.vander.core.domain.queue
import kotlinx.coroutines.flow.StateFlow
import org.vander.core.domain.data.CurrentlyPlaying

/**
 * The playback queue as the Web API sees it.
 *
 * Same shape as [PlaylistRepository]: [refresh] both returns and publishes, and
 * `null` on [currentQueue] means "not loaded yet".
 *
 * The queue is only a snapshot — it does not update on its own when the track changes, so it
 * has to be re-fetched.
 */
interface QueueRepository {
    val currentQueue: StateFlow<CurrentlyPlaying?>

    suspend fun refresh(): Result<Unit>
}
