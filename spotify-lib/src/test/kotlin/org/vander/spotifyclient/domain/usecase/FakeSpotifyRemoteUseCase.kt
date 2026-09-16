package org.vander.spotifyclient.domain.usecase

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.vander.core.domain.data.CurrentlyPlaying

/**
 * Serves [nextQueue] each time the queue is fetched, and counts the fetches — which is what a
 * test needs to tell a single refetch from a loop.
 */
class FakeSpotifyRemoteUseCase : SpotifyRemoteUseCase {
    private val _currentUserQueue = MutableStateFlow<CurrentlyPlaying?>(null)
    override val currentUserQueue: StateFlow<CurrentlyPlaying?> = _currentUserQueue.asStateFlow()

    /** What the next fetch publishes; `null` publishes nothing, as a failed fetch would. */
    var nextQueue: CurrentlyPlaying? = null

    var fetchCount: Int = 0
        private set

    override suspend fun getAndEmitUserQueueFlow() {
        fetchCount++
        nextQueue?.let { _currentUserQueue.value = it }
    }
}
