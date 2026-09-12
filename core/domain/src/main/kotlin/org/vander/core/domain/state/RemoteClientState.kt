package org.vander.core.domain.state

/**
 * Connection state of the App Remote client itself, below [SessionState].
 *
 * Unlike [PlayerConnectionState] it carries the failure cause, so a caller can decide
 * between a retry and surfacing the error.
 */
sealed class RemoteClientState {
    object NotConnected : RemoteClientState()

    object Connecting : RemoteClientState()

    object Connected : RemoteClientState()

    data class Failed(
        val error: Exception,
    ) : RemoteClientState()
}
