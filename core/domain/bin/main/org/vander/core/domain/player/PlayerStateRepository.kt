package org.vander.core.domain.player

import kotlinx.coroutines.flow.StateFlow
import org.vander.core.domain.state.PlayerStateData
import org.vander.core.domain.state.SavedRemotelyChangedState

interface PlayerStateRepository {
    val playerStateData: StateFlow<PlayerStateData>
    val savedRemotelyChangedState: StateFlow<SavedRemotelyChangedState>

    suspend fun startListening()

    suspend fun stopListening()
}
