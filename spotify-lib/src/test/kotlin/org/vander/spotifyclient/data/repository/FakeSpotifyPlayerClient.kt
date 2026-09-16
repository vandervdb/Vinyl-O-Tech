package org.vander.spotifyclient.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.vander.core.domain.data.PlaybackContext
import org.vander.core.domain.data.SpotifyUri
import org.vander.core.domain.state.PlayerConnectionState
import org.vander.core.domain.state.PlayerStateData
import org.vander.spotifyclient.domain.player.PlayerClient

/**
 * [PlayerClient] driven by the test: [emit] and [emitContext] push as the App Remote would,
 * [commands] records what was sent, [nextResult] decides how it answers.
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

    private var contextListener: ((PlaybackContext) -> Unit)? = null

    override suspend fun subscribeToPlayerContext(function: (PlaybackContext) -> Unit) {
        contextListener = function
    }

    override fun unsubscribeFromPlayerContext() {
        contextListener = null
    }

    /** Pushes a context to whoever subscribed, as the App Remote would. */
    fun emitContext(context: PlaybackContext) {
        contextListener?.invoke(context)
    }

    /** Last URI handed to [play], so a test can assert what the layers above built. */
    var lastPlayed: SpotifyUri? = null
        private set

    /** Every transport command received, in order, by name — `"seekTo:4200"` for arguments. */
    val commands = mutableListOf<String>()

    /** What every transport command answers; set a failure to simulate a refusal. */
    var nextResult: Result<Unit> = Result.success(Unit)

    override suspend fun play(uri: SpotifyUri): Result<Unit> {
        lastPlayed = uri
        return record("play")
    }

    override suspend fun pause(): Result<Unit> = record("pause")

    override suspend fun resume(): Result<Unit> = record("resume")

    override suspend fun skipNext(): Result<Unit> = record("skipNext")

    override suspend fun skipPrevious(): Result<Unit> = record("skipPrevious")

    override suspend fun seekTo(position: Long): Result<Unit> = record("seekTo:$position")

    private fun record(command: String): Result<Unit> {
        commands += command
        return nextResult
    }

    override fun setShuffle(shuffle: Boolean) {}

    override fun setRepeat(repeat: Int) {}

    override fun isPlaying(): Boolean = !lastState.value.isPaused

    fun emit(state: PlayerStateData) {
        _lastState.value = state
        listener?.invoke(state)
    }
}
