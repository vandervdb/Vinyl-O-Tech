package org.vander.core.domain.player

import kotlinx.coroutines.flow.StateFlow
import org.vander.core.domain.data.PlaybackContext
import org.vander.core.domain.state.PlayerStateData
import org.vander.core.domain.state.SavedRemotelyChangedState

/**
 * Subscribes to the Spotify App Remote player and republishes its state as hot flows.
 *
 * [startListening] and [stopListening] bracket the subscription and must be paired —
 * the underlying remote subscription outlives a single screen, so ownership sits in the
 * repository rather than in a ViewModel.
 */
interface PlayerStateRepository {
    /** Latest player snapshot; starts at [PlayerStateData.empty]. */
    val playerStateData: StateFlow<PlayerStateData>

    /** Last "track saved" change observed from another device. */
    val savedRemotelyChangedState: StateFlow<SavedRemotelyChangedState>

    /**
     * What playback is running from, on the App Remote's own channel — it does not change
     * at the same moments as [playerStateData]. Starts at [PlaybackContext.None].
     */
    val playbackContext: StateFlow<PlaybackContext>

    suspend fun startListening()

    suspend fun stopListening()
}
