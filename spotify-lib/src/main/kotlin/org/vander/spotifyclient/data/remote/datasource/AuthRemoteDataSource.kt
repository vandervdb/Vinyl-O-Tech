package org.vander.spotifyclient.data.remote.datasource

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Parameters
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.vander.core.dto.TokenResponseDto
import org.vander.core.logger.Logger
import org.vander.spotifyclient.BuildConfig.CLIENT_ID
import org.vander.spotifyclient.BuildConfig.CLIENT_SECRET
import org.vander.spotifyclient.domain.auth.IAuthRemoteDatasource
import org.vander.spotifyclient.utils.REDIRECT_URI
import javax.inject.Inject
import javax.inject.Named
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class AuthRemoteDataSource
    @Inject
    constructor(
        @param:Named("AuthHttpClient") val httpClient: HttpClient,
        private val logger: Logger,
    ) : IAuthRemoteDatasource {
        @OptIn(ExperimentalEncodingApi::class)
        override suspend fun fetchAccessToken(code: String): Result<TokenResponseDto> {
            return try {
                val credentials = "$CLIENT_ID:$CLIENT_SECRET"
                val encodedCredentials = Base64.encode(credentials.toByteArray())
                logger.d("AuthRemoteDataSource", "encodedCredentials: $encodedCredentials")
                logger.d("AuthRemoteDataSource", "CLIENT_ID: $CLIENT_ID")
                val maskedSecret =
                    if (CLIENT_SECRET.length > 4) {
                        CLIENT_SECRET.substring(0, 2) + "****" + CLIENT_SECRET.substring(CLIENT_SECRET.length - 2)
                    } else {
                        "****"
                    }
                logger.d("AuthRemoteDataSource", "CLIENT_SECRET: $maskedSecret")
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
                logger.d("AuthRemoteDataSource", "Raw body: $rawBody")

                if (response.status.value in 400..499) {
                    try {
                        val json = Json { ignoreUnknownKeys = true }
                        val errorObj = json.parseToJsonElement(rawBody).jsonObject
                        if (errorObj.containsKey("error_description")) {
                            val description = errorObj["error_description"].toString()
                            logger.e("AuthRemoteDataSource", "Spotify error description: $description")
                        }
                        if (errorObj.containsKey("error")) {
                            val error = errorObj["error"].toString()
                            logger.e("AuthRemoteDataSource", "Spotify error code: $error")
                        }
                    } catch (e: Exception) {
                        // ignore parsing error for description
                    }
                }

                if (response.status.value == 200) {
                    val json = Json { ignoreUnknownKeys = true }
                    return Result.success(json.decodeFromString<TokenResponseDto>(rawBody))
                } else {
                    return Result.failure(Exception("Spotify error ${response.status.value}: $rawBody"))
                }
            } catch (e: Exception) {
                logger.e("AuthRemoteDataSource", "Error fetching token", e)
                Result.failure(e)
            }
        }
    }
