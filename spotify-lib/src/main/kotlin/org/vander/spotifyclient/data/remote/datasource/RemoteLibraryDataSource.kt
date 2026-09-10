package org.vander.spotifyclient.data.remote.datasource

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import kotlinx.serialization.json.Json
import org.vander.core.domain.auth.ITokenProvider
import org.vander.core.logger.Logger
import org.vander.spotifyclient.domain.datasource.IRemoteLibraryDataSource
import javax.inject.Inject
import javax.inject.Named

class RemoteLibraryDataSource
    @Inject
    constructor(
        @Named("auth_api_v1_client") private val httpClient: HttpClient,
        private val tokenProvider: ITokenProvider,
        private val logger: Logger,
    ) : IRemoteLibraryDataSource {
        override suspend fun fetchIsTrackSaved(trackId: String): Result<Boolean> =
            try {
                val token = tokenProvider.getAccessToken().orEmpty()
                val response =
                    httpClient.get("me/tracks/contains") {
                        url { parameters.append("ids", trackId) }
                        headers { append(HttpHeaders.Authorization, "Bearer $token") }
                    }
                val body = response.bodyAsText()
                val isSaved =
                    Json { ignoreUnknownKeys = true }
                        .decodeFromString<List<Boolean>>(body)
                        .firstOrNull() == true
                Result.success(isSaved)
            } catch (e: Exception) {
                logger.e("RemoteLibraryDataSource", "Error checking if track is saved", e)
                Result.failure(e)
            }

        override suspend fun saveTrack(trackId: String): Result<Unit> =
            try {
                val token = tokenProvider.getAccessToken().orEmpty()
                httpClient.put("me/tracks") {
                    url { parameters.append("ids", trackId) }
                    headers { append(HttpHeaders.Authorization, "Bearer $token") }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                logger.e("RemoteLibraryDataSource", "Error saving track $trackId", e)
                Result.failure(e)
            }

        override suspend fun removeTrack(trackId: String): Result<Unit> =
            try {
                val token = tokenProvider.getAccessToken().orEmpty()
                httpClient.delete("me/tracks") {
                    url { parameters.append("ids", trackId) }
                    headers { append(HttpHeaders.Authorization, "Bearer $token") }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                logger.e("RemoteLibraryDataSource", "Error removing track $trackId", e)
                Result.failure(e)
            }
    }
