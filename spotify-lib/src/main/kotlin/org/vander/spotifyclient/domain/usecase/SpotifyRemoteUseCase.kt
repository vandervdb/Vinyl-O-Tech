package org.vander.spotifyclient.domain.usecase

import kotlinx.coroutines.flow.StateFlow
import org.vander.core.domain.data.CurrentlyPlaying

interface SpotifyRemoteUseCase {
    val currentUserQueue: StateFlow<CurrentlyPlaying?>

    suspend fun getAndEmitUserQueueFlow()
}
