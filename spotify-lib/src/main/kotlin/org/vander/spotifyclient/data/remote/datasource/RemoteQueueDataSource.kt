package org.vander.spotifyclient.data.remote.datasource

import org.vander.core.dto.CurrentlyPlayingWithQueueDto

/** Raw `me/player/queue` call, returning the DTO untouched. */
internal fun interface RemoteQueueDataSource {
    suspend fun fetchUserQueue(): Result<CurrentlyPlayingWithQueueDto>
}
