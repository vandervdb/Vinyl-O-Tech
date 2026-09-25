package org.vander.spotifyclient.data.player

import com.spotify.android.appremote.api.PlayerApi
import com.spotify.protocol.client.CallResult
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.Empty
import com.spotify.protocol.types.PlayerContext
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import org.vander.core.domain.data.PlaybackContext
import org.vander.core.domain.data.SpotifyUri
import org.vander.core.domain.state.PlayerConnectionState
import org.vander.core.domain.state.PlayerStateData
import org.vander.core.logger.Logger
import org.vander.spotifyclient.data.player.mapper.toPlaybackContext
import org.vander.spotifyclient.data.player.mapper.toPlayerStateData
import org.vander.spotifyclient.domain.appremote.AppRemoteProvider
import org.vander.spotifyclient.domain.player.PlayerClient
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds

/**
 * A client for interacting with the Spotify player.
 *
 * This class provides methods for controlling playback, subscribing to player state updates,
 * and managing the connection to the Spotify app.
 *
 * @property appRemoteProvider An [AppRemoteProvider] instance used to access the Spotify App Remote.
 * @property logger A [Logger] instance for logging debug messages.
 */
class SpotifyPlayerClient
    @Inject
    constructor(
        private val appRemoteProvider: AppRemoteProvider,
        private val logger: Logger,
    ) : PlayerClient {
        companion object {
            const val TAG = "SpotifyPlayerClient"

            private const val COMMAND_TIMEOUT_MS = 5_000L
        }

        private var isPlaying = false
        private val playerApi: PlayerApi?
            get() = appRemoteProvider.get()?.playerApi

        private val _playerConnectionState =
            MutableStateFlow<PlayerConnectionState>(PlayerConnectionState.NotConnected)
        override val playerConnectionState: StateFlow<PlayerConnectionState> =
            _playerConnectionState.asStateFlow()

        private val _lastState = MutableStateFlow(PlayerStateData.empty())
        override val lastState: StateFlow<PlayerStateData> = _lastState.asStateFlow()

        private var stateSubscription: Subscription<PlayerState>? = null

        private var contextSubscription: Subscription<PlayerContext>? = null

        override suspend fun subscribeToPlayerState(function: (PlayerStateData) -> Unit) {
            playerApi?.let { api ->
                stateSubscription =
                    api.subscribeToPlayerState().setEventCallback { state ->
                        val track: Track = state.track
                        logger.d(
                            TAG,
                            "PlayerClient received new data: " + track.name + " by " + track.artist.name +
                                "(paused: " + state.isPaused + " / coverUri: " + track.imageUri + ")",
                        )
                        isPlaying = !state.isPaused
                        _lastState.value = state.toPlayerStateData(logger)
                        function(state.toPlayerStateData(logger))
                    }
            } ?: run {
                logger.e(TAG, "spotifyPlayerApi is null")
                _playerConnectionState.update { PlayerConnectionState.NotConnected }
            }
        }

        override suspend fun play(uri: SpotifyUri): Result<Unit> {
            logger.d(TAG, "play uri: $uri")
            return command("play") { play(uri.value) }
        }

        override suspend fun pause(): Result<Unit> = command("pause") { pause() }

        override suspend fun resume(): Result<Unit> = command("resume") { resume() }

        override suspend fun skipNext(): Result<Unit> = command("skipNext") { skipNext() }

        override suspend fun skipPrevious(): Result<Unit> = command("skipPrevious") { skipPrevious() }

        override suspend fun seekTo(position: Long): Result<Unit> = command("seekTo") { seekTo(position) }

        /**
         * Sends one command and suspends until the App Remote answers.
         *
         * The SDK reports through two callbacks on a `CallResult`; this turns them into a
         * [Result]. Two guards: a missing `PlayerApi` fails at once instead of dropping the call,
         * and [COMMAND_TIMEOUT_MS] bounds the wait, since a remote that dies mid-call never calls
         * either callback back. Cancelling the caller cancels the SDK call too.
         */
        private suspend fun command(
            name: String,
            call: PlayerApi.() -> CallResult<Empty>,
        ): Result<Unit> {
            val api = playerApi
            if (api == null) {
                logger.e(TAG, "$name: spotifyPlayerApi is null")
                return Result.failure(IllegalStateException("$name: PlayerApi unavailable"))
            }

            val outcome =
                withTimeoutOrNull(COMMAND_TIMEOUT_MS.milliseconds) {
                    suspendCancellableCoroutine { continuation ->
                        val pending = api.call()
                        pending.setResultCallback {
                            if (continuation.isActive) {
                                continuation.resume(
                                    Result.success(Unit),
                                )
                            }
                        }
                        pending.setErrorCallback { if (continuation.isActive) continuation.resume(Result.failure(it)) }
                        continuation.invokeOnCancellation { pending.cancel() }
                    }
                }
                    ?: Result.failure(
                        TimeoutException("$name: no answer from the App Remote within ${COMMAND_TIMEOUT_MS}ms"),
                    )

            outcome
                .onSuccess { logger.d(TAG, "$name: accepted") }
                .onFailure { logger.e(TAG, "$name: failed", it) }
            return outcome
        }

        override fun setShuffle(shuffle: Boolean) {
            playerApi
                ?.setShuffle(shuffle)
                ?.setResultCallback { logger.d(TAG, "setShuffle: accepted") }
                ?.setErrorCallback { logger.e(TAG, "setShuffle: failed", it) }
                ?: logger.e(TAG, "setShuffle: spotifyPlayerApi is null")
        }

        override fun setRepeat(repeat: Int) {
            playerApi
                ?.setRepeat(repeat)
                ?.setResultCallback { logger.d(TAG, "setRepeat: accepted") }
                ?.setErrorCallback { logger.e(TAG, "setRepeat: failed", it) }
                ?: logger.e(TAG, "setRepeat: spotifyPlayerApi is null")
        }

        override fun isPlaying(): Boolean = isPlaying

        override suspend fun subscribeToPlayerContext(function: (PlaybackContext) -> Unit) {
            playerApi?.let { api ->
                contextSubscription =
                    api.subscribeToPlayerContext().setEventCallback { context ->
                        val playbackContext = context.toPlaybackContext()
                        logger.d(TAG, "PlayerClient received new context: $playbackContext")
                        function(playbackContext)
                    }
            } ?: logger.e(TAG, "subscribeToPlayerContext: spotifyPlayerApi is null")
        }

        override fun unsubscribeFromPlayerContext() {
            contextSubscription?.cancel()
            contextSubscription = null
        }

        override fun unsubscribeFromPlayerState() {
            // Cancels the subscription that was opened, not a fresh one: calling
            // subscribeToPlayerState() here would open a second one and cancel that instead,
            // leaving the first running.
            stateSubscription?.cancel()
            stateSubscription = null
            appRemoteProvider.disconnect()
        }
    }
