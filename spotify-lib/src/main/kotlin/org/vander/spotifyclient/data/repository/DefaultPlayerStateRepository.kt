package org.vander.spotifyclient.data.repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.vander.core.domain.player.PlayerStateRepository
import org.vander.core.domain.state.PlayerStateData
import org.vander.core.domain.state.SavedRemotelyChangedState
import org.vander.core.logger.Logger
import org.vander.spotifyclient.domain.player.PlayerClient
import javax.inject.Inject

/**
 * Republishes the App Remote player state, and infers the "saved elsewhere" signal from it.
 *
 * That inference is the subtle part, and it rests on an assumption this class makes about the
 * SDK rather than on a documented guarantee: a push carrying a state equal to the previous one
 * is read as "something the SDK does not model changed", i.e. the track being saved from
 * another device. It is published on [savedRemotelyChangedState], then immediately reset to
 * `false` so it behaves as a one-shot event rather than a level. Any other cause of a
 * duplicate push would produce a false positive.
 *
 * `isListening` makes [startListening] idempotent. Note that [stopListening] only clears that
 * flag — it does not unsubscribe from the player.
 */
class DefaultPlayerStateRepository
    @Inject
    constructor(
        private val playerClient: PlayerClient,
        private val logger: Logger,
    ) : PlayerStateRepository {
        companion object {
            private const val TAG = "DefaultPlayerStateRepository"
        }

        private val _playerStateData = MutableStateFlow(PlayerStateData.Companion.empty())
        override val playerStateData: StateFlow<PlayerStateData> = _playerStateData.asStateFlow()

        private val _savedRemotelyChangedState =
            MutableStateFlow(SavedRemotelyChangedState())
        override val savedRemotelyChangedState: StateFlow<SavedRemotelyChangedState> =
            _savedRemotelyChangedState.asStateFlow()

        private var isListening = false

        override suspend fun startListening() {
            if (isListening) return
            isListening = true
            playerClient.subscribeToPlayerState { newState ->
                if (newState == _playerStateData.value) {
                    logger.d(TAG, "Player state did not change -> saved status changed")
                    _savedRemotelyChangedState.update {
                        SavedRemotelyChangedState(
                            true,
                            newState.trackId,
                        )
                    }
                    _savedRemotelyChangedState.update { SavedRemotelyChangedState(false) } // reset
                }
                _playerStateData.update { newState }
            }
        }

        override suspend fun stopListening() {
            isListening = false
        }
    }
