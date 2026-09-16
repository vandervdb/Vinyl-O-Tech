package org.vander.spotifyclient.domain.player

import kotlinx.coroutines.flow.StateFlow
import org.vander.core.domain.player.PlayerCommand
import org.vander.core.domain.state.PlaybackState

/**
 * The player, seen from a ViewModel: one state to read, one entry point to act.
 *
 * Deliberately narrow. What it replaces exposed six collaborators, five flows and a method per
 * action, so every consumer had to know which flow was fed by what and in which order to call
 * things. Behind these three members sit the App Remote subscriptions, the Web API queue and
 * the saved-track lookups; none of that leaks through.
 *
 * Bound as a singleton: every screen reads the same state. A second instance would compile and
 * run, and silently publish nothing — which is exactly what happened before it was scoped.
 */
interface PlayerController {
    /** Starts at [PlaybackState]'s defaults; filled once a session is ready and [start] ran. */
    val state: StateFlow<PlaybackState>

    /**
     * Starts observing the session, the player and the queue. Idempotent: the first call
     * launches the work, later calls do nothing, so any screen may call it.
     */
    fun start()

    /**
     * Executes [command].
     *
     * @return a failure when the player refused or could not be reached — a free account asked
     *   to play, a disconnected App Remote. The outcome of a success comes back through [state].
     */
    suspend fun dispatch(command: PlayerCommand): Result<Unit>
}
