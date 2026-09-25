package org.vander.spotifyclient.data.remote.datasource

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import org.vander.core.dto.CurrentlyPlayingWithQueueDto
import org.vander.spotifyclient.utils.parseSpotifyResult
import javax.inject.Inject
import javax.inject.Named

/**
 * `GET me/player/queue`. Parsing and error mapping are delegated to `parseSpotifyResult`.
 *
 * Authentication belongs to the client, not here: `auth_api_v1_client` installs
 * `AuthHeaderPlugin`, which reads the token once per request and therefore picks up a
 * refresh on its own.
 */
internal class SpotifyRemoteQueueDataSource
    @Inject
    constructor(
        @param:Named("auth_api_v1_client") private val httpClient: HttpClient,
    ) : RemoteQueueDataSource {
        override suspend fun fetchUserQueue(): Result<CurrentlyPlayingWithQueueDto> =
            try {
                httpClient.get("me/player/queue").parseSpotifyResult<CurrentlyPlayingWithQueueDto>(TAG)
            } catch (e: Exception) {
                Result.failure(e)
            }

        private companion object {
            const val TAG = "SpotifyRemoteQueueDataSource"
        }
    }
