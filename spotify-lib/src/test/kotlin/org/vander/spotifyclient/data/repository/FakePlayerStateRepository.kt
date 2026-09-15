package org.vander.spotifyclient.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.vander.core.domain.data.PlaybackContext
import org.vander.core.domain.player.PlayerStateRepository
import org.vander.core.domain.state.PlayerStateData
import org.vander.core.domain.state.SavedRemotelyChangedState

class FakePlayerStateRepository : PlayerStateRepository {
    private val _playerStateData = MutableStateFlow(PlayerStateData.Companion.empty())
    override val playerStateData: StateFlow<PlayerStateData> = _playerStateData.asStateFlow()

    private val _savedRemotelyChangedState =
        MutableStateFlow<SavedRemotelyChangedState>(SavedRemotelyChangedState())
    override val savedRemotelyChangedState: StateFlow<SavedRemotelyChangedState> =
        _savedRemotelyChangedState.asStateFlow()

    private val _playbackContext = MutableStateFlow(PlaybackContext.None)
    override val playbackContext: StateFlow<PlaybackContext> = _playbackContext.asStateFlow()

    /** Pushes a context, as the App Remote would. */
    fun emitContext(context: PlaybackContext) {
        _playbackContext.value = context
    }

    override suspend fun startListening() {
        TODO("Not yet implemented")
    }

    override suspend fun stopListening() {
        TODO("Not yet implemented")
    }
}
