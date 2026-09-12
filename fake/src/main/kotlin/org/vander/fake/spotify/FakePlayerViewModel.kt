package org.vander.fake.spotify

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.vander.core.domain.state.DomainPlayerState
import org.vander.core.domain.state.PlayerStateData
import org.vander.core.domain.state.SessionState
import org.vander.core.ui.presentation.viewmodel.PlayerViewModel
import org.vander.core.ui.state.UIQueueState

/**
 * [PlayerViewModel] backed by static data, for `@Preview` and design work — no Hilt graph,
 * no App Remote, no network.
 *
 * Renders a paused track with a full-length progress bar. Actions are accepted but most of
 * them do not move the state yet (see the `TODO`s below), so a preview that depends on a
 * play/pause toggle actually changing will not show it.
 */
class FakePlayerViewModel : PlayerViewModel {
    // Backing properties
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Ready)
    override val sessionState: StateFlow<SessionState> get() = _sessionState.asStateFlow()

    private val _uiQueueState = MutableStateFlow(UIQueueState.empty())
    override val uiQueueState: StateFlow<UIQueueState> get() = _uiQueueState.asStateFlow()

    private val _domainPlayerState =
        MutableStateFlow(
            DomainPlayerState(
                base =
                    PlayerStateData(
                        trackName = "Zelda's Theme",
                        artistName = "Koji Kondo",
                        albumName = "Zelda",
                        coverId = "",
                        trackId = "",
                        isPaused = true,
                        playing = false,
                        paused = true,
                        stopped = false,
                        shuffling = false,
                        repeating = true,
                        seeking = false,
                        skippingNext = false,
                        skippingPrevious = false,
                        positionMs = 1234567890L,
                        durationMs = 1234567890L,
                    ),
                isTrackSaved = false,
            ),
        )
    override val domainPlayerState: StateFlow<DomainPlayerState> get() = _domainPlayerState.asStateFlow()

    override fun startUp() {
        // TODO: initialise si besoin (_sessionState.value = SessionState.Ready, etc.)
    }

    override fun togglePlayPause() {
        // TODO: ex. _domainPlayerState.update { it.togglePause() }
    }

    override fun skipNext() {
        // TODO
    }

    override fun skipPrevious() {
        // TODO
    }

    override fun playTrack(trackId: String) {
        // TODO
    }

    override fun checkIfTrackSaved(trackId: String) {
        // Nothing to do (fake)
    }

    override fun toggleSaveTrack(trackId: String) {
        // Nothing to do (fake)
    }

    override fun seekTo(position: Long) {
        // Nothing to do (fake)
    }
}
