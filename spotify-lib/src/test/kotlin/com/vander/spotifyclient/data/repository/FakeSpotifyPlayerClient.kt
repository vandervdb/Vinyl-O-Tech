package com.vander.spotifyclient.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.vander.core.domain.state.PlayerConnectionState
import org.vander.core.domain.state.PlayerStateData
import org.vander.spotifyclient.domain.player.PlayerClient

/**
 * Fake implementation of PlayerClient for testing DefaultPlayerStateRepository.
 */
class FakeSpotifyPlayerClient : PlayerClient {
    private val _playerConnectionState =
        MutableStateFlow<PlayerConnectionState>(PlayerConnectionState.NotConnected)
    override val playerConnectionState: StateFlow<PlayerConnectionState>
        get() = _playerConnectionState.asStateFlow()

    private val _lastState = MutableStateFlow(PlayerStateData.empty())
    override val lastState: StateFlow<PlayerStateData>
        get() = _lastState.asStateFlow()

    private var listener: ((PlayerStateData) -> Unit)? = null

    override suspend fun subscribeToPlayerState(function: (PlayerStateData) -> Unit) {
        listener = function
    }

    override fun unsubscribeFromPlayerState() {
        listener = null
    }

    override suspend fun play(trackUri: String) {}

    override suspend fun pause() {}

    override suspend fun resume() {}

    override suspend fun skipNext() {}

    override suspend fun skipPrevious() {}

    override fun seekTo(position: Long) {}

    override fun setShuffle(shuffle: Boolean) {}

    override fun setRepeat(repeat: Int) {}

    override fun isPlaying(): Boolean = !lastState.value.isPaused

    fun emit(state: PlayerStateData) {
        _lastState.value = state
        listener?.invoke(state)
    }
}
