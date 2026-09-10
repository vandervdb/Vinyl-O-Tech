package org.vander.spotifyclient.data.remote.datasource

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import org.vander.core.domain.auth.ITokenProvider
import org.vander.core.dto.SpotifyPlaylistsResponseDto
import org.vander.spotifyclient.domain.datasource.IRemotePlaylistDataSource
import org.vander.spotifyclient.utils.parseSpotifyResult
import javax.inject.Inject
import javax.inject.Named

class RemotePlaylistDataSource
    @Inject
    constructor(
        @Named("auth_api_v1_client") private val httpClient: HttpClient,
        private val tokenProvider: ITokenProvider,
    ) : IRemotePlaylistDataSource {
        override suspend fun fetchUserPlaylists(): Result<SpotifyPlaylistsResponseDto> {
            val token = tokenProvider.getAccessToken() ?: ""
            return try {
                val response =
                    httpClient.get("me/playlists") {
                        headers {
                            append(HttpHeaders.Authorization, "Bearer $token")
                        }
                    }
                return response.parseSpotifyResult<SpotifyPlaylistsResponseDto>("SpotifyRemoteDataSource")
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
