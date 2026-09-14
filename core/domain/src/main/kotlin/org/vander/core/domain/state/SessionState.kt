package org.vander.core.domain.state

/**
 * Lifecycle of a Spotify session, from cold start to a usable App Remote connection.
 *
 * Modelled as a sealed class so a `when` over it is exhaustive: adding a step here breaks
 * every consumer at compile time instead of silently falling through an `else`.
 *
 * Nominal order: [Idle] -> [Authorizing] -> [ConnectingRemote] -> [Ready].
 * [Failed] can replace any of them.
 */
sealed class SessionState {
    object Idle : SessionState()

    object Authorizing : SessionState()

    object ConnectingRemote : SessionState()

    object Ready : SessionState()

    data class Failed(
        val exception: Throwable,
    ) : SessionState()
}
