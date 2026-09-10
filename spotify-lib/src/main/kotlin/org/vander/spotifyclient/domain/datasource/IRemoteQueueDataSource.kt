package org.vander.spotifyclient.domain.datasource

import org.vander.core.dto.CurrentlyPlayingWithQueueDto

fun interface IRemoteQueueDataSource {
    suspend fun fetchUserQueue(): Result<CurrentlyPlayingWithQueueDto>
}
