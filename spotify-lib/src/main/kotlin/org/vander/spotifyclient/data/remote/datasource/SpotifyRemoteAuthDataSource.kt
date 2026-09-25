package org.vander.spotifyclient.data.remote.datasource

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Parameters
import kotlinx.serialization.json.jsonObject
import org.vander.core.dto.TokenResponseDto
import org.vander.core.logger.Logger
import org.vander.spotifyclient.BuildConfig.CLIENT_ID
import org.vander.spotifyclient.BuildConfig.CLIENT_SECRET
import org.vander.spotifyclient.utils.REDIRECT_URI
import org.vander.spotifyclient.utils.spotifyJson
import javax.inject.Inject
import javax.inject.Named
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
        @OptIn(ExperimentalEncodingApi::class)
        override suspend fun fetchAccessToken(code: String): Result<TokenResponseDto> {
            return try {
                val credentials = "$CLIENT_ID:$CLIENT_SECRET"
                val encodedCredentials = Base64.encode(credentials.toByteArray())
                logger.d("SpotifyRemoteAuthDataSource", "encodedCredentials: $encodedCredentials")
                logger.d("SpotifyRemoteAuthDataSource", "CLIENT_ID: $CLIENT_ID")
                val maskedSecret =
                    if (CLIENT_SECRET.length > 4) {
                        CLIENT_SECRET.substring(0, 2) + "****" + CLIENT_SECRET.substring(CLIENT_SECRET.length - 2)
                    } else {
                        "****"
                    }
                logger.d("SpotifyRemoteAuthDataSource", "CLIENT_SECRET: $maskedSecret")
                val response =
                    httpClient.submitForm(
                        url = "token",
                        formParameters =
                            Parameters.build {
                                append("grant_type", "authorization_code")
                                append("code", code)
                                append("redirect_uri", REDIRECT_URI)
                            },
                    ) {
                        headers {
                            append("Authorization", "Basic ${encodedCredentials.trim()}")
                        }
                    }

                val rawBody = response.bodyAsText()
                logger.d("SpotifyRemoteAuthDataSource", "Raw body: $rawBody")

                if (response.status.value in 400..499) {
                    try {
                        val errorObj = spotifyJson.parseToJsonElement(rawBody).jsonObject
                        if (errorObj.containsKey("error_description")) {
                            val description = errorObj["error_description"].toString()
                            logger.e("SpotifyRemoteAuthDataSource", "Spotify error description: $description")
                        }
                        if (errorObj.containsKey("error")) {
                            val error = errorObj["error"].toString()
                            logger.e("SpotifyRemoteAuthDataSource", "Spotify error code: $error")
                        }
                    } catch (e: Exception) {
                        // ignore parsing error for description
                    }
                }

                if (response.status.value == 200) {
                    return Result.success(spotifyJson.decodeFromString<TokenResponseDto>(rawBody))
                } else {
                    return Result.failure(Exception("Spotify error ${response.status.value}: $rawBody"))
                }
            } catch (e: Exception) {
                logger.e("SpotifyRemoteAuthDataSource", "Error fetching token", e)
                Result.failure(e)
            }
        }
    }
