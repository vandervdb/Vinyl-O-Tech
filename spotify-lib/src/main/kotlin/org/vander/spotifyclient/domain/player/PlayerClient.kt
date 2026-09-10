package org.vander.spotifyclient.domain.player

import kotlinx.coroutines.flow.StateFlow
import org.vander.core.domain.state.PlayerConnectionState
import org.vander.core.domain.state.PlayerStateData

interface PlayerClient {
    val playerConnectionState: StateFlow<PlayerConnectionState>
    val lastState: StateFlow<PlayerStateData>

    suspend fun subscribeToPlayerState(function: (PlayerStateData) -> Unit)

    fun unsubscribeFromPlayerState()

    suspend fun play(trackUri: String)

    suspend fun pause()

    suspend fun resume()

    suspend fun skipNext()

    suspend fun skipPrevious()

    fun seekTo(position: Long)

    fun setShuffle(shuffle: Boolean)

    fun setRepeat(repeat: Int) // RepeatMode.OFF = 0, RepeatMode.ONE = 1, RepeatMode.ALL = 2

    fun isPlaying(): Boolean
}
