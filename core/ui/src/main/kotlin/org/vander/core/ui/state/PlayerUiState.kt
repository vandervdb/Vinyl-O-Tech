package org.vander.core.ui.state

import org.vander.core.domain.data.PlaybackContext
import org.vander.core.domain.state.DomainPlayerState
import org.vander.core.domain.state.SessionState

/**
 * Everything a player screen draws, in one value.
 *
 * One snapshot, so a screen never reads values that belong to different moments, and a fake
 * can implement the contract in a few lines.
 *
 * @property session connection lifecycle with the App Remote.
 * @property player current track, playback position and saved flag.
 * @property queue current track first, then what comes next, already mapped for display.
 * @property context what playback is running from.
 */
data class PlayerUiState(
    val session: SessionState = SessionState.Idle,
    val player: DomainPlayerState = DomainPlayerState.empty(),
    val queue: UIQueueState = UIQueueState.empty(),
    val context: PlaybackContext = PlaybackContext.None,
)
