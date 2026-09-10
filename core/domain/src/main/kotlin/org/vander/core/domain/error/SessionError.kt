package org.vander.core.domain.error

/**
 * Represents a sealed class for session-related errors.
 * This class encapsulates different types of errors that can occur during a session.
 *
 * @param message A detailed message about the error, if available.
 * @param cause The underlying cause of the error, if available.
 */
sealed class SessionError(
    message: String? = null,
    cause: Throwable?,
) : Exception(message) {
    data class AuthFailed(
        override val cause: Throwable? = null,
    ) : SessionError("Spotify Authorization Failed", cause)

    data class RemoteConnectionFailed(
        override val cause: Throwable? = null,
    ) : SessionError("Spotify Remote Connection Failed", cause)

    data class UnknownError(
        override val cause: Throwable? = null,
    ) : SessionError("Unknown error occurred", cause)
}
