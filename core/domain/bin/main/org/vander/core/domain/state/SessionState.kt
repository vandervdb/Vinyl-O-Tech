package org.vander.core.domain.state

sealed class SessionState {
    object Idle : SessionState()

    object Authorizing : SessionState()

    object ConnectingRemote : SessionState()

    object Ready : SessionState()

    object IsPaused : SessionState()

    data class Failed(
        val exception: Throwable,
    ) : SessionState()
}
