package org.vander.spotifyclient.data.remote.datasource

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import org.vander.core.logger.Logger
import org.vander.spotifyclient.utils.spotifyJson
import javax.inject.Inject
import javax.inject.Named

/**
 * The `me/tracks` endpoints: read the saved flag, save, remove.
 *
 * `me/tracks/contains` answers with a JSON array aligned on the requested ids, so the single
 * id sent here is read back as the first element.
 *
 * Authentication belongs to the client: `auth_api_v1_client` installs `AuthHeaderPlugin`,
 * which reads the token once per request.
 *
 * The two writes answer with an empty body, so there is nothing to parse and nothing that
 * would fail on its own — [orFailure] is what turns a refusal into a failed [Result]. Ktor's
 * `expectSuccess` is off, so without it a 403 would be reported as a success.
 */
internal class SpotifyRemoteLibraryDataSource
    @Inject
    constructor(
        @param:Named("auth_api_v1_client") private val httpClient: HttpClient,
        private val logger: Logger,
    ) : RemoteLibraryDataSource {
        override suspend fun fetchIsTrackSaved(trackId: String): Result<Boolean> =
            try {
                val response =
                    httpClient.get("me/tracks/contains") {
                        url { parameters.append("ids", trackId) }
                    }
                val isSaved =
                    spotifyJson
                        .decodeFromString<List<Boolean>>(response.bodyAsText())
                        .firstOrNull() == true
                Result.success(isSaved)
            } catch (e: Exception) {
                logger.e(TAG, "Error checking if track is saved", e)
                Result.failure(e)
            }

        override suspend fun saveTrack(trackId: String): Result<Unit> =
            try {
                httpClient
                    .put("me/tracks") { url { parameters.append("ids", trackId) } }
                    .orFailure()
            } catch (e: Exception) {
                Result.failure(e)
            }.onFailure { logger.e(TAG, "Error saving track $trackId", it) }

        override suspend fun removeTrack(trackId: String): Result<Unit> =
            try {
                httpClient
                    .delete("me/tracks") { url { parameters.append("ids", trackId) } }
                    .orFailure()
            } catch (e: Exception) {
                Result.failure(e)
            }.onFailure { logger.e(TAG, "Error removing track $trackId", it) }

        /**
         * Turns a non-2xx answer into a failed [Result], since Ktor does not throw on one
         * with `expectSuccess` off.
         */
        private fun HttpResponse.orFailure(): Result<Unit> =
            if (status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException("Spotify answered ${status.value} ${status.description}"))
            }

        private companion object {
            const val TAG = "SpotifyRemoteLibraryDataSource"
        }
    }
