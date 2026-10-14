package org.vander.spotifyclient.data.player

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.vander.core.domain.player.PlayerCommand
import org.vander.core.domain.player.PlayerController
import org.vander.core.domain.player.PlayerStateRepository
import org.vander.core.domain.session.SessionManager
import org.vander.core.domain.state.PlaybackState
import org.vander.core.domain.state.SessionState
import org.vander.core.logger.Logger
import org.vander.spotifyclient.di.ApplicationScope
import org.vander.spotifyclient.domain.player.PlayerClient
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * [org.vander.core.domain.player.PlayerController] over the App Remote and the Web API.
 *
 * It merges three sources that move at different rhythms into one [org.vander.core.domain.state.PlaybackState]: the App
 * Remote pushes a snapshot on every tick and a context on its own channel, while the queue and
 * the saved flag are Web API snapshots that have to be fetched. Each source has its own
 * collector, launched once by [start] in the injected [scope] — a process-wide scope in
 * production, so a screen going away does not stop what other screens read.
 *
 * The Web API halves have their own owners: [QueueSynchronizer] for the queue and how it copes
 * with the Web API lagging behind the App Remote, [SavedFlagTracker] for the saved flag. The
 * controller runs them and assembles their state.
 */
class SpotifyPlayerController
    @Inject
    internal constructor(
        private val sessionManager: SessionManager,
        private val queueSynchronizer: QueueSynchronizer,
        private val savedFlagTracker: SavedFlagTracker,
        private val playerStateRepository: PlayerStateRepository,
        private val playerClient: PlayerClient,
        @param:ApplicationScope private val scope: CoroutineScope,
        private val logger: Logger,
    ) : PlayerController {
        private val _state = MutableStateFlow(PlaybackState())
        override val state: StateFlow<PlaybackState> = _state.asStateFlow()

        private val started = AtomicBoolean(false)

        override fun start() {
            if (!started.compareAndSet(false, true)) return
            logger.d(TAG, "Starting")
            scope.launch { observeSession() }
            scope.launch { observePlayer() }
            scope.launch { savedFlagTracker.run() }
            scope.launch { queueSynchronizer.run() }
            scope.launch { observeQueue() }
            scope.launch { observeContext() }
        }

        override suspend fun dispatch(command: PlayerCommand): Result<Unit> =
            when (command) {
                PlayerCommand.TogglePlayPause -> {
                    if (playerClient.isPlaying()) {
                        playerClient.pause()
                    } else {
                        playerClient
                            .resume()
                    }
                }

                PlayerCommand.Pause -> {
                    playerClient.pause()
                }

                PlayerCommand.Resume -> {
                    playerClient.resume()
                }

                PlayerCommand.SkipNext -> {
                    playerClient.skipNext()
                }

                PlayerCommand.SkipPrevious -> {
                    playerClient.skipPrevious()
                }

                is PlayerCommand.SeekTo -> {
                    playerClient.seekTo(command.positionMs)
                }

                is PlayerCommand.Play -> {
                    playerClient.play(command.uri)
                }

                PlayerCommand.ToggleSave -> {
                    savedFlagTracker.toggle()
                }
            }.onFailure { logger.e(TAG, "dispatch($command) failed", it) }

        private suspend fun observeSession() {
            sessionManager.sessionState.collect { session ->
                if (session !is SessionState.Ready) {
                    // The subscriptions belong to the connection that just went away: without
                    // this, the next Ready finds the repository still "listening" and skips
                    // subscribing to the new connection.
                    playerStateRepository.stopListening()
                    return@collect
                }
                logger.d(TAG, "Session ready")
                queueSynchronizer.refresh()
                playerStateRepository.startListening()
            }
        }

        private suspend fun observePlayer() {
            // Combined in one update so a new track never shows, even for a frame, with the
            // previous track's flag: a flag only applies while its own track is playing.
            combine(playerStateRepository.playerStateData, savedFlagTracker.flag) { snapshot, flag ->
                snapshot to flag?.takeIf { it.trackId == snapshot.trackId }?.isSaved
            }.collect { (snapshot, isTrackSaved) ->
                _state.update { it.copy(player = it.player.copy(base = snapshot, isTrackSaved = isTrackSaved)) }
            }
        }

        private suspend fun observeQueue() {
            queueSynchronizer.queue.collect { queue ->
                _state.update { it.copy(queue = queue) }
            }
        }

        private suspend fun observeContext() {
            playerStateRepository.playbackContext.collect { context ->
                _state.update { it.copy(context = context) }
            }
        }

        private companion object {
            const val TAG = "SpotifyPlayerController"
        }
    }
