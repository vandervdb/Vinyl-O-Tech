package org.vander.spotifyclient.data.player

import com.spotify.android.appremote.api.PlayerApi
import com.spotify.protocol.types.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.vander.core.domain.state.PlayerConnectionState
import org.vander.core.domain.state.PlayerStateData
import org.vander.core.logger.Logger
import org.vander.spotifyclient.data.player.mapper.toPlayerStateData
import org.vander.spotifyclient.domain.appremote.AppRemoteProvider
import org.vander.spotifyclient.domain.player.PlayerClient
import javax.inject.Inject

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

        override suspend fun subscribeToPlayerState(function: (PlayerStateData) -> Unit) {
            playerApi?.let { api ->
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

        override suspend fun play(trackUri: String) {
            logger.d(TAG, "play trackUri: $trackUri")
            playerApi
                ?.play(trackUri)
                ?.setResultCallback { logger.d(TAG, "play: accepted") }
                ?.setErrorCallback { logger.e(TAG, "play: failed", it) }
                ?: logger.e(TAG, "play: spotifyPlayerApi is null")
        }

        override suspend fun pause() {
            playerApi
                ?.pause()
                ?.setResultCallback { logger.d(TAG, "pause: accepted") }
                ?.setErrorCallback { logger.e(TAG, "pause: failed", it) }
                ?: logger.e(TAG, "pause: spotifyPlayerApi is null")
        }

        override suspend fun resume() {
            logger.d(TAG, "resume: ")
            playerApi
                ?.resume()
                ?.setResultCallback { logger.d(TAG, "resume: accepted") }
                ?.setErrorCallback { logger.e(TAG, "resume: failed", it) }
                ?: logger.e(TAG, "resume: spotifyPlayerApi is null")
        }

        override suspend fun skipNext() {
            playerApi
                ?.skipNext()
                ?.setResultCallback { logger.d(TAG, "skipNext: accepted") }
                ?.setErrorCallback { logger.e(TAG, "skipNext: failed", it) }
                ?: logger.e(TAG, "skipNext: spotifyPlayerApi is null")
        }

        override suspend fun skipPrevious() {
            playerApi
                ?.skipPrevious()
                ?.setResultCallback { logger.d(TAG, "skipPrevious: accepted") }
                ?.setErrorCallback { logger.e(TAG, "skipPrevious: failed", it) }
                ?: logger.e(TAG, "skipPrevious: spotifyPlayerApi is null")
        }

        override fun seekTo(position: Long) {
            playerApi
                ?.seekTo(position)
                ?.setResultCallback { logger.d(TAG, "seekTo: accepted") }
                ?.setErrorCallback { logger.e(TAG, "seekTo: failed", it) }
                ?: logger.e(TAG, "seekTo: spotifyPlayerApi is null")
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

        override fun unsubscribeFromPlayerState() {
            playerApi?.subscribeToPlayerState()?.cancel()
            appRemoteProvider.disconnect()
        }
    }
