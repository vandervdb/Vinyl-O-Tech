package org.vander.spotifyclient.domain.datasource

import org.vander.core.dto.CurrentlyPlayingWithQueueDto

/** Raw `me/player/queue` call, returning the DTO untouched. */
fun interface IRemoteQueueDataSource {
    suspend fun fetchUserQueue(): Result<CurrentlyPlayingWithQueueDto>
}
