package org.vander.spotifyclient.domain.usecase

import kotlinx.coroutines.flow.StateFlow
import org.vander.core.domain.data.CurrentlyPlaying

/**
 * Keeps a hot view of the playback queue, refreshed on demand.
 *
 * Interface rather than a concrete class so a test of [org.vander.spotifyclient.domain.usecase.PlayerUseCase]
 * can drive the queue without a network stack.
 */
interface SpotifyRemoteUseCase {
    val currentUserQueue: StateFlow<CurrentlyPlaying?>

    /**
     * Fetches the queue and publishes it on [currentUserQueue]. Despite the name it returns
     * nothing and creates no flow; a failure leaves the previous value in place.
     */
    suspend fun getAndEmitUserQueueFlow()
}
