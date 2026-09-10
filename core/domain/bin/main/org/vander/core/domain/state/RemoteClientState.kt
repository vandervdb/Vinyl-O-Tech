package org.vander.core.domain.state

sealed class RemoteClientState {
    object NotConnected : RemoteClientState()

    object Connecting : RemoteClientState()

    object Connected : RemoteClientState()

    data class Failed(
        val error: Exception,
    ) : RemoteClientState()
}
