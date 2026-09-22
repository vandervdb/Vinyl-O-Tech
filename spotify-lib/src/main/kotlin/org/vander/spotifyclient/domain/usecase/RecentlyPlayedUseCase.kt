package org.vander.spotifyclient.domain.usecase

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.vander.core.domain.data.RecentlyPlayed
import org.vander.core.logger.Logger
import org.vander.spotifyclient.domain.repository.RecentlyPlayedRepository
import javax.inject.Inject

class RecentlyPlayedUseCase
    @Inject
    constructor(
        val recentlyPlayedRepository: RecentlyPlayedRepository,
        private val logger: Logger,
    ) {
        companion object Companion {
            private const val TAG = "RecentlyPlayedUseCase"
        }

        private val _recentlyPlayed = MutableStateFlow(RecentlyPlayed.empty())
        val recentlyPlayed: StateFlow<RecentlyPlayed> = _recentlyPlayed.asStateFlow()

        suspend fun getAndUpdateRecentlyPlayedFlow() {
            recentlyPlayedRepository.getRecentlyPlayed().fold(
                { recentlyPlayed ->
                    logger.d(TAG, "Received user recently played: $recentlyPlayed")
                    _recentlyPlayed.update { recentlyPlayed }
                },
                { error ->
                    logger.e(TAG, "Error occurred while fetching user recently played", error)
                },
            )
        }
    }
