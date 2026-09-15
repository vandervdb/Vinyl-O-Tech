package org.vander.spotifyclient.data.remote.datasource

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import org.vander.core.dto.SpotifyPlaylistsResponseDto
import org.vander.spotifyclient.domain.datasource.IRemotePlaylistDataSource
import org.vander.spotifyclient.utils.parseSpotifyResult
import javax.inject.Inject
import javax.inject.Named

/**
 * `GET me/playlists`. Parsing and error mapping are delegated to `parseSpotifyResult`.
 *
 * Authentication belongs to the client, not here: `auth_api_v1_client` installs
 * `AuthHeaderPlugin`, which reads the token once per request and therefore picks up a
 * refresh on its own.
 */
class RemotePlaylistDataSource
    @Inject
    constructor(
        @param:Named("auth_api_v1_client") private val httpClient: HttpClient,
    ) : IRemotePlaylistDataSource {
        override suspend fun fetchUserPlaylists(): Result<SpotifyPlaylistsResponseDto> =
            try {
                httpClient.get("me/playlists").parseSpotifyResult<SpotifyPlaylistsResponseDto>(TAG)
            } catch (e: Exception) {
                Result.failure(e)
            }

        private companion object {
            const val TAG = "RemotePlaylistDataSource"
        }
    }
