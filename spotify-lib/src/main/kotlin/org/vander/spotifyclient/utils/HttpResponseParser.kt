package org.vander.spotifyclient.utils

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.vander.core.dto.ErrorResponseDto
import org.vander.core.logger.Logger
import org.vander.core.logger.NoOpLogger

suspend inline fun <reified T> HttpResponse.parseSpotifyResult(
    tag: String = "SpotifyApi",
    logger: Logger,
): Result<T> {
    val rawBody = this.bodyAsText()
    val json = Json { ignoreUnknownKeys = true }

    return try {
        val root = json.parseToJsonElement(rawBody).jsonObject

        if ("error" in root) {
            val errorDto = json.decodeFromString<ErrorResponseDto>(rawBody)
            logger.e(tag, "Spotify error ${errorDto.error.status} : ${errorDto.error.message}")
            Result.failure(Exception("Spotify error ${errorDto.error.status}: ${errorDto.error.message}"))
        } else {
            logger.d(tag, "Spotify response: $rawBody")
            val result = json.decodeFromString<T>(rawBody)
            Result.success(result)
        }
    } catch (e: Exception) {
        logger.e(tag, "JSON parsing error", e)
        Result.failure(e)
    }
}

suspend inline fun <reified T> HttpResponse.parseSpotifyResult(tag: String = "SpotifyApi"): Result<T> {
    val defaultLogger: Logger = NoOpLogger()
    return this.parseSpotifyResult<T>(tag, defaultLogger)
}
