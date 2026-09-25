package org.vander.spotifyclient.data.remote.datasource

import org.vander.core.dto.RecentlyPlayedResponseDto

internal fun interface RemoteRecentlyPlayedDataSource {
    suspend fun fetchRecentlyPlayed(): Result<RecentlyPlayedResponseDto>
}
