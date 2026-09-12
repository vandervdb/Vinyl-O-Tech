package org.vander.core.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response of the Spotify accounts token endpoint.
 *
 * @property expiresIn token lifetime in seconds from the moment it was issued — a duration,
 *   not an absolute date, so the expiry must be computed at reception.
 * @property refreshToken only returned by the flows that grant one.
 */
@Serializable
data class TokenResponseDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Int,
    @SerialName("refresh_token") val refreshToken: String? = null,
)
