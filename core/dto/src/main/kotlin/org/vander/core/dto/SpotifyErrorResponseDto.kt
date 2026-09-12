package org.vander.core.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Error envelope returned by the Web API on a non-2xx response: the payload is always
 * wrapped in a single `error` object.
 */
@Serializable
data class ErrorResponseDto(
    @SerialName("error") val error: ErrorDetailDto,
)

@Serializable
data class ErrorDetailDto(
    @SerialName("status") val status: Int,
    @SerialName("message") val message: String,
)
