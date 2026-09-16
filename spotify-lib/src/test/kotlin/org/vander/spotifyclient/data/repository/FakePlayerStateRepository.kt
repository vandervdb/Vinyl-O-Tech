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

    var startListeningCount: Int = 0
        private set

    /** Pushes a player snapshot, as the App Remote would. */
    fun emitState(state: PlayerStateData) {
        _playerStateData.value = state
    }

    /** Signals a track saved or unsaved from another device. */
    fun emitSavedRemotely(event: SavedRemotelyChangedState) {
        _savedRemotelyChangedState.value = event
    }

    override suspend fun startListening() {
        startListeningCount++
    }

    override suspend fun stopListening() = Unit
}
