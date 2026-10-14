package org.vander.spotifyclient.data.remote.datasource

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.isSuccess
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.vander.core.dto.TokenResponseDto
import org.vander.core.logger.Logger
import org.vander.spotifyclient.BuildConfig.CLIENT_ID
import org.vander.spotifyclient.BuildConfig.CLIENT_SECRET
import org.vander.spotifyclient.utils.REDIRECT_URI
import org.vander.spotifyclient.utils.spotifyJson
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.cancellation.CancellationException
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Exchanges the authorization code for a token against the Spotify accounts service.
 *
 * It uses its own `@Named("AuthHttpClient")` Ktor client, without the bearer plugin: this
 * call authenticates with a Basic header built from the client id and secret, not with a
 * token — and sending the app's own credentials to the API client would be wrong.
 *
 * Warning: this method logs the raw response body and the Base64 credentials at debug level,
 * so an access token and the client secret end up in logcat on a debug build.
 */
internal class SpotifyRemoteAuthDataSource
    @Inject
    constructor(
        @param:Named("AuthHttpClient") val httpClient: HttpClient,
        private val logger: Logger,
    ) : RemoteAuthDataSource {
        override suspend fun fetchAccessToken(code: String): Result<TokenResponseDto> =
            requestToken(
                action = "exchanging the authorization code",
                form =
                    Parameters.build {
                        append("grant_type", "authorization_code")
                        append("code", code)
                        append("redirect_uri", REDIRECT_URI)
                    },
            )

        override suspend fun refreshAccessToken(refreshToken: String): Result<TokenResponseDto> =
            requestToken(
                action = "refreshing the access token",
                form =
                    Parameters.build {
                        append("grant_type", "refresh_token")
                        append("refresh_token", refreshToken)
                    },
            )

        private suspend fun requestToken(
            action: String,
            form: Parameters,
        ): Result<TokenResponseDto> =
            try {
                val response =
                    httpClient.submitForm(url = "token", formParameters = form) {
                        header(HttpHeaders.Authorization, basicCredentials())
                    }
                val rawBody = response.bodyAsText()
                if (response.status.isSuccess()) {
                    Result.success(spotifyJson.decodeFromString<TokenResponseDto>(rawBody))
                } else {
                    val message = "Spotify accounts error ${response.status.value}: ${describeError(rawBody)}"
                    logger.e(TAG, "Error $action — $message")
                    Result.failure(IllegalStateException(message))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.e(TAG, "Error $action", e)
                Result.failure(e)
            }

        @OptIn(ExperimentalEncodingApi::class)
        private fun basicCredentials(): String = "Basic " + Base64.encode("$CLIENT_ID:$CLIENT_SECRET".toByteArray())

        // The accounts service uses a flat {"error", "error_description"} envelope, unlike the
        // Web API's nested one, so parseSpotifyResult cannot read it.
        private fun describeError(rawBody: String): String =
            runCatching {
                val error = spotifyJson.parseToJsonElement(rawBody).jsonObject
                listOfNotNull(
                    error["error"]?.jsonPrimitive?.content,
                    error["error_description"]?.jsonPrimitive?.content,
                ).joinToString(": ")
            }.getOrNull()?.takeIf { it.isNotEmpty() } ?: rawBody

        companion object {
            private const val TAG = "SpotifyRemoteAuthDataSource"
        }
    }
