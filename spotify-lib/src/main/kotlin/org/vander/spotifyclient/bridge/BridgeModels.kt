package org.vander.spotifyclient.bridge

data class AuthConfigK(
    val clientId: String,
    val redirectUrl: String,
    val scopes: Array<String>,
    val showDialog: Boolean = true,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AuthConfigK) return false

        if (showDialog != other.showDialog) return false
        if (clientId != other.clientId) return false
        if (redirectUrl != other.redirectUrl) return false
        if (!scopes.contentEquals(other.scopes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = showDialog.hashCode()
        result = 31 * result + clientId.hashCode()
        result = 31 * result + redirectUrl.hashCode()
        result = 31 * result + scopes.contentHashCode()
        return result
    }
}

sealed class AuthResult {
    data class Authenticated(
        val accessToken: String,
    ) : AuthResult()

    data class Failed(
        val reason: Reason,
        val cause: Throwable? = null,
    ) : AuthResult()

    enum class Reason {
        TIMEOUT,
        SESSION_FAILED,
        TOKEN_MISSING,
        UNEXPECTED,
    }
}

data class PlayerStateDto(
    val isPlaying: Boolean,
    val positionMs: Long,
    val durationMs: Long,
    val trackUri: String? = null,
    val coverId: String? = null,
    val trackName: String? = null,
    val artistName: String? = null,
    val albumName: String? = null,
)
