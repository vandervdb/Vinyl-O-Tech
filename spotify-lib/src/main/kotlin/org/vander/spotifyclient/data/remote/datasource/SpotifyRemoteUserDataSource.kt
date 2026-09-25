package org.vander.spotifyclient.data.remote.datasource

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import org.vander.core.dto.UserDto
import org.vander.spotifyclient.utils.parseSpotifyResult
import javax.inject.Inject
import javax.inject.Named

/**
 * `GET me`. Parsing and error mapping are delegated to `parseSpotifyResult`.
 *
 * Authentication belongs to the client, not here: `auth_api_v1_client` installs
 * `AuthHeaderPlugin`, which reads the token once per request and therefore picks up a
 * refresh on its own.
 */
internal class SpotifyRemoteUserDataSource
    @Inject
    constructor(
        @param:Named("auth_api_v1_client") private val httpClient: HttpClient,
    ) : RemoteUserDataSource {
        override suspend fun fetchUser(): Result<UserDto> =
            try {
                httpClient.get("me").parseSpotifyResult<UserDto>(TAG)
            } catch (e: Exception) {
                Result.failure(e)
            }

        private companion object {
            const val TAG = "SpotifyRemoteUserDataSource"
        }
    }
