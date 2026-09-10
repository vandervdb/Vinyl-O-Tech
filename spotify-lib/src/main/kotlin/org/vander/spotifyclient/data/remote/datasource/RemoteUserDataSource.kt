package org.vander.spotifyclient.data.remote.datasource

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import org.vander.core.domain.auth.ITokenProvider
import org.vander.core.dto.UserDto
import org.vander.spotifyclient.domain.datasource.IRemoteUserDataSource
import org.vander.spotifyclient.utils.parseSpotifyResult
import javax.inject.Inject
import javax.inject.Named

class RemoteUserDataSource
    @Inject
    constructor(
        @param:Named("auth_api_v1_client") private val httpClient: HttpClient,
        private val tokenProvider: ITokenProvider,
    ) : IRemoteUserDataSource {
        override suspend fun fetchUser(): Result<UserDto> {
            val token = tokenProvider.getAccessToken().orEmpty()
            return try {
                val response =
                    httpClient.get("me") {
                        headers {
                            append(HttpHeaders.Authorization, "Bearer $token")
                        }
                    }
                return response.parseSpotifyResult<UserDto>("RemoteUserDataSource")
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
