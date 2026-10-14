package org.vander.spotifyclient.domain.auth

internal data class SessionTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long,
) {
    fun isAccessTokenExpired(
        now: Long,
        margin: Long = EXPIRY_MARGIN_MS,
    ): Boolean = now >= expiresAt - margin

    private companion object {
        const val EXPIRY_MARGIN_MS = 60_000L
    }
}
