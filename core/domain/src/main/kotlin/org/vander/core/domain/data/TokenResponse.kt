package org.vander.core.domain.data

/**
 * The token endpoint's answer, normalized for the app.
 *
 * Differs from `TokenResponseDto` on the one thing the wire format cannot express:
 * [expiresAt] is an absolute instant, where the API sends `expires_in`, a duration counted
 * from the moment the token was issued. `token_type` is dropped — it is always `Bearer` and
 * nothing in the app reads it.
 *
 * @property expiresAt epoch milliseconds, computed at reception by the data layer's mapper.
 * @property refreshToken null on a refresh grant: Spotify returns one only when it issues a
 *   new one, so null means "keep the stored one", never "there is none".
 */
data class TokenResponse(
    val accessToken: String,
    val expiresAt: Long,
    val refreshToken: String?,
)
