package org.vander.core.domain.state

import org.vander.core.domain.data.PlaybackContext
import org.vander.core.domain.data.QueuedTrack

/**
 * Everything the player publishes, in one value.
 *
 * Replaces the three flows a consumer used to collect separately — player state, queue,
 * context — so a screen reads one consistent snapshot instead of combining streams that
 * move at different moments.
 *
 * @property player the App Remote snapshot and the saved flag.
 * @property queue the current track first, then what comes next; empty until the Web API
 *   queue agrees with the track the App Remote reports.
 * @property context what playback is running from.
 */
data class PlaybackState(
    val player: DomainPlayerState = DomainPlayerState.empty(),
    val queue: List<QueuedTrack> = emptyList(),
    val context: PlaybackContext = PlaybackContext.None,
)
