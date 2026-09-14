package org.vander.spotifyclient.domain.usecase

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.vander.core.domain.data.CurrentlyPlaying

class FakeSpotifyRemoteUseCase : SpotifyRemoteUseCase {
    private val _currentUserQueue = MutableStateFlow<CurrentlyPlaying?>(null)
    override val currentUserQueue: StateFlow<CurrentlyPlaying?> = _currentUserQueue.asStateFlow()

    var requested: Boolean = false

    override suspend fun getAndEmitUserQueueFlow() {
        requested = true
    }

    fun emitQueue(value: CurrentlyPlaying?) {
        if (requested) _currentUserQueue.value = value
    }
}
