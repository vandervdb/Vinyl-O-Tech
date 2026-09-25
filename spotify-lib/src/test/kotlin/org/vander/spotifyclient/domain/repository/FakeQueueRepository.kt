package org.vander.spotifyclient.domain.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.vander.core.domain.data.CurrentlyPlaying
import org.vander.core.domain.queue.QueueRepository
import java.io.IOException

/**
 * Serves [nextQueue] on each refresh and counts the refreshes — which is what a test needs to
 * tell a single refetch from a loop. `null` publishes nothing and fails, as a network error would.
 */
class FakeQueueRepository : QueueRepository {
    private val _currentQueue = MutableStateFlow<CurrentlyPlaying?>(null)
    override val currentQueue: StateFlow<CurrentlyPlaying?> = _currentQueue.asStateFlow()

    var nextQueue: CurrentlyPlaying? = null

    var refreshCount: Int = 0
        private set

    override suspend fun refresh(): Result<Unit> {
        refreshCount++
        val queue = nextQueue ?: return Result.failure(IOException("no queue"))
        _currentQueue.value = queue
        return Result.success(Unit)
    }
}
