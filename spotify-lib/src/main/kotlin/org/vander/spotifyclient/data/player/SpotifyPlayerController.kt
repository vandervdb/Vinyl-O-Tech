package org.vander.spotifyclient.data.player

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.vander.core.domain.data.QueuedTrack
import org.vander.core.domain.data.Track
import org.vander.core.domain.library.LibraryRepository
import org.vander.core.domain.player.PlayerCommand
import org.vander.core.domain.player.PlayerController
import org.vander.core.domain.player.PlayerStateRepository
import org.vander.core.domain.queue.QueueRepository
import org.vander.core.domain.session.SessionManager
import org.vander.core.domain.state.PlaybackState
import org.vander.core.domain.state.PlayerStateData
import org.vander.core.domain.state.SessionState
import org.vander.core.logger.Logger
import org.vander.spotifyclient.di.ApplicationScope
import org.vander.spotifyclient.domain.player.PlayerClient
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * [org.vander.core.domain.player.PlayerController] over the App Remote and the Web API.
 *
 * It merges three sources that move at different rhythms into one [org.vander.core.domain.state.PlaybackState]: the App
 * Remote pushes a snapshot on every tick and a context on its own channel, while the queue and
 * the saved flag are Web API snapshots that have to be fetched. Each source has its own
 * collector, launched once by [start] in the injected [scope] — a process-wide scope in
 * production, so a screen going away does not stop what other screens read.
 *
 * Two rules keep the Web API calls bounded:
 * - the saved flag is looked up when the track changes, not on every tick;
 * - a lagging queue is refetched on a track change, at most [QUEUE_RETRY_DELAYS] times, never
 *   on a queue emission: the Web API lags behind the App Remote after a skip, and a refetch
 *   triggered by the queue it updates would loop.
 */
class SpotifyPlayerController
    @Inject
    constructor(
        private val sessionManager: SessionManager,
        private val queueRepository: QueueRepository,
        private val playerStateRepository: PlayerStateRepository,
        private val libraryRepository: LibraryRepository,
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
            scope.launch { observeSavedFlag() }
            scope.launch { observeSavedElsewhere() }
            scope.launch { observeQueue() }
            scope.launch { observeCurrentQueueLag() }
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
                    toggleSave()
                }
            }.onFailure { logger.e(TAG, "dispatch($command) failed", it) }

        /**
         * Persists the change, then publishes it. The flag only moves on success, so a refused
         * call leaves the heart as it was rather than showing a state the library does not have.
         */
        private suspend fun toggleSave(): Result<Unit> {
            val player = _state.value.player
            val trackId = player.base.trackId
            if (trackId.isEmpty()) return Result.failure(IllegalStateException("ToggleSave: no track loaded"))

            val wasSaved = player.isTrackSaved == true
            val result = if (wasSaved) libraryRepository.removeTrack(trackId) else libraryRepository.saveTrack(trackId)
            return result.onSuccess { publishSavedFlag(trackId, !wasSaved) }
        }

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
                queueRepository
                    .refresh()
                    .onFailure { logger.e(TAG, "Queue refetch failed", it) }
                playerStateRepository.startListening()
            }
        }

        private suspend fun observePlayer() {
            playerStateRepository.playerStateData.collect { snapshot ->
                _state.update { current ->
                    val trackChanged = snapshot.trackId != current.player.base.trackId
                    // A flag belongs to one track: carrying it over would show the previous
                    // track's heart until the new lookup answers.
                    val isTrackSaved = if (trackChanged) null else current.player.isTrackSaved
                    current.copy(player = current.player.copy(base = snapshot, isTrackSaved = isTrackSaved))
                }
            }
        }

        private suspend fun observeSavedFlag() {
            playerStateRepository.playerStateData
                .map { it.trackId }
                .filter { it.isNotEmpty() }
                .distinctUntilChanged()
                .collect { trackId -> lookUpSavedFlag(trackId) }
        }

        private suspend fun observeSavedElsewhere() {
            playerStateRepository.savedRemotelyChangedState
                .filter { it.isSaved && it.trackId.isNotEmpty() }
                .collect { event -> lookUpSavedFlag(event.trackId) }
        }

        private suspend fun lookUpSavedFlag(trackId: String) {
            libraryRepository
                .isTrackSaved(trackId)
                .onSuccess { publishSavedFlag(trackId, it) }
                .onFailure { logger.e(TAG, "Saved flag lookup failed for $trackId", it) }
        }

        /** Ignores an answer that arrives after the track changed. */
        private fun publishSavedFlag(
            trackId: String,
            isSaved: Boolean,
        ) {
            _state.update { current ->
                if (current.player.base.trackId != trackId) return@update current
                current.copy(player = current.player.copy(isTrackSaved = isSaved))
            }
        }

        private suspend fun observeQueue() {
            combine(queueRepository.currentQueue, playerStateRepository.playerStateData, ::Pair)
                .collect { (queue, snapshot) ->
                    if (queue == null || snapshot.trackId.isEmpty()) return@collect

                    if (queue.currentlyPlaying?.id == snapshot.trackId) {
                        _state.update { it.copy(queue = queueOf(snapshot, queue.queue.tracks)) }
                        return@collect
                    }

                    // Until the Web API catches up, keep the queue's head on the track that is
                    // actually playing: a stale head shows a title that no longer matches the cover.
                    val upcoming = queue.queue.tracks
                    val playingIndex = upcoming.indexOfFirst { it.id == snapshot.trackId }
                    val stillUpcoming = if (playingIndex >= 0) upcoming.drop(playingIndex + 1) else emptyList()
                    _state.update { it.copy(queue = queueOf(snapshot, stillUpcoming)) }
                }
        }

        private suspend fun observeCurrentQueueLag() {
            playerStateRepository.playerStateData
                .map { it.trackId }
                .filter { it.isNotEmpty() }
                .distinctUntilChanged()
                .collectLatest { trackId ->
                    for (wait in QUEUE_RETRY_DELAYS) {
                        if (isQueueInStepWith(trackId)) return@collectLatest
                        delay(wait)
                        logger.d(
                            TAG,
                            "Queue lagging behind $trackId " +
                                "(api=${queueRepository.currentQueue.value?.currentlyPlaying?.id}), refetching",
                        )
                        queueRepository
                            .refresh()
                            .onFailure { logger.e(TAG, "Queue refetch failed", it) }
                    }
                    if (!isQueueInStepWith(trackId)) {
                        logger.w(TAG, "Queue still lagging behind $trackId, giving up until the next track")
                    }
                }
        }

        private fun isQueueInStepWith(trackId: String): Boolean =
            queueRepository.currentQueue.value
                ?.currentlyPlaying
                ?.id == trackId

        private suspend fun observeContext() {
            playerStateRepository.playbackContext.collect { context ->
                _state.update { it.copy(context = context) }
            }
        }

        /**
         * The playing track first — taken from the App Remote, which is ahead of the Web API —
         * then the upcoming tracks. A null slot, mapped to `Track.empty()` upstream, has no
         * artist: `firstOrNull` keeps it from throwing.
         */
        private fun queueOf(
            snapshot: PlayerStateData,
            upcoming: List<Track>,
        ): List<QueuedTrack> =
            listOf(QueuedTrack(snapshot.trackId, snapshot.trackName, snapshot.artistName)) +
                upcoming.map { track ->
                    QueuedTrack(
                        id = track.id,
                        name = track.name,
                        artistName =
                            track.artists
                                .firstOrNull()
                                ?.name
                                .orEmpty(),
                    )
                }

        private companion object {
            const val TAG = "SpotifyPlayerController"

            // The Web API answers with the previous track for a few hundred milliseconds after
            // a skip: an immediate refetch almost always comes back stale.
            val QUEUE_RETRY_DELAYS = listOf(500.milliseconds, 1.seconds, 2.seconds)
        }
    }
