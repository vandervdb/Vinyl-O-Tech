package org.vander.spotifyclient.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.vander.core.domain.data.CurrentlyPlaying
import org.vander.core.domain.queue.QueueRepository
import org.vander.spotifyclient.data.remote.datasource.RemoteQueueDataSource
import org.vander.spotifyclient.data.remote.mapper.toDomain
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Fetches the playback queue, maps it to the domain model and caches the result in memory.
 *
 * Same shape as [SpotifyPlaylistRepository]; the cached value survives a failed refresh.
 */
internal class SpotifyQueueRepository
    @Inject
    constructor(
        private val api: RemoteQueueDataSource,
    ) : QueueRepository {
        private val _currentQueue = MutableStateFlow<CurrentlyPlaying?>(null)
        override val currentQueue: StateFlow<CurrentlyPlaying?> = _currentQueue.asStateFlow()

        override suspend fun refresh(): Result<Unit> =
            try {
                val dto = api.fetchUserQueue().getOrThrow()
                val result = dto.toDomain()
                _currentQueue.update { result }
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
    }
