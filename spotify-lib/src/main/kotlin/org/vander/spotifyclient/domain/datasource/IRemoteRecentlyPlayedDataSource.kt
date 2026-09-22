package org.vander.spotifyclient.domain.datasource

import org.vander.core.dto.RecentlyPlayedResponseDto

interface IRemoteRecentlyPlayedDataSource {
    suspend fun fetchRecentlyPlayed(): Result<RecentlyPlayedResponseDto>
}
