package org.vander.spotifyclient.bridge

/**
 * Authorization parameters a host can override; when it passes `null`, the library falls
 * back to its own client id, redirect URI and scope list.
 *
 * [equals] and [hashCode] are hand-written because [scopes] is an `Array`, whose generated
 * implementations compare identity rather than content — two configs with identical scopes
 * would otherwise never be equal.
 *
 * @property showDialog forces the Spotify approval screen even when the user already granted
 *   the scopes.
 */
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

/**
 * Outcome of an authorization started through the bridge.
 *
 * [Failed.reason] is an enum rather than free text so a host can branch on it — typically to
 * retry on `TIMEOUT` but not on `SESSION_FAILED`.
 */
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

/**
 * Player state flattened for a host, with every field nullable so a partial state still
 * crosses the boundary.
 *
 * @property trackUri holds the bare track id despite its name — see `toPlayerStateDto`.
 */
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
