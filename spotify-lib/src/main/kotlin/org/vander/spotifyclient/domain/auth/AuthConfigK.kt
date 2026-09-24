package org.vander.spotifyclient.domain.auth

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
