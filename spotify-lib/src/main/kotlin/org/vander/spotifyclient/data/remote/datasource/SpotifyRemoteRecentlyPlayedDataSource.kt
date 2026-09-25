package org.vander.spotifyclient.data.remote.datasource

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import org.vander.core.dto.RecentlyPlayedResponseDto
import org.vander.spotifyclient.utils.parseSpotifyResult
import javax.inject.Inject
import javax.inject.Named

internal class SpotifyRemoteRecentlyPlayedDataSource
    @Inject
    constructor(
        @param:Named("auth_api_v1_client") private val httpClient: HttpClient,
    ) : RemoteRecentlyPlayedDataSource {
        override suspend fun fetchRecentlyPlayed(): Result<RecentlyPlayedResponseDto> =
            try {
                return httpClient.get("me/player/recently-played").parseSpotifyResult<RecentlyPlayedResponseDto>(TAG)
            } catch (e: Exception) {
                return Result.failure(e)
            }

        private companion object {
            const val TAG = "SpotifyRemotePlaylistDataSource"
        }
    }
