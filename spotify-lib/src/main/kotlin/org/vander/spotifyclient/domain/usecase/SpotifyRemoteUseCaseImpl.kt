package org.vander.spotifyclient.domain.usecase

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.vander.core.domain.data.CurrentlyPlaying
import org.vander.core.logger.Logger
import org.vander.spotifyclient.domain.repository.SpotifyQueueRepository
import javax.inject.Inject

class SpotifyRemoteUseCaseImpl
    @Inject
    constructor(
        private val queueRepository: SpotifyQueueRepository,
        private val logger: Logger,
    ) : SpotifyRemoteUseCase {
        companion object {
            private const val TAG = "SpotifyRemoteUseCase"
        }

        private val _currentUserQueue = MutableStateFlow<CurrentlyPlaying?>(null)
        override val currentUserQueue: StateFlow<CurrentlyPlaying?> = _currentUserQueue.asStateFlow()

        override suspend fun getAndEmitUserQueueFlow() {
            queueRepository.getUserQueue().fold(
                onSuccess = { currentUserQueue ->
                    logger.d(TAG, "Received user queue: $currentUserQueue")
                    _currentUserQueue.update { currentUserQueue }
                },
                onFailure = { exception ->
                    logger.e(TAG, "Error getting user queue", exception)
                },
            )
        }
    }
