package org.vander.core.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponseDto(
    @SerialName("error") val error: ErrorDetailDto,
)

@Serializable
data class ErrorDetailDto(
    @SerialName("status") val status: Int,
    @SerialName("message") val message: String,
)
