package org.vander.core.ui.presentation.viewmodel

import kotlinx.coroutines.flow.StateFlow
import org.vander.core.domain.player.PlayerCommand
import org.vander.core.ui.state.PlayerUiState

/**
 * Contract every player ViewModel must honour, real or fake.
 *
 * Implemented by the app's `@HiltViewModel` and by [org.vander.fake.spotify.FakePlayerViewModel]
 * for `@Preview`. It lives in `core-ui` rather than in `app` so both sides compile against the
 * same surface without `app` depending on `fake` at runtime — dependency inversion applied to
 * the presentation layer.
 *
 * Two members, where there used to be eleven: one state to draw, one entry point for what the
 * user does. A new action is a new [PlayerCommand], not a new method on every implementation —
 * which is what left the fake with empty bodies and previews that never reacted.
 */
interface PlayerViewModel {
    val state: StateFlow<PlayerUiState>

    /** Fire-and-forget: the outcome comes back through [state], never as a return value. */
    fun onCommand(command: PlayerCommand)
}
