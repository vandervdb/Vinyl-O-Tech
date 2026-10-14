package org.vander.spotifyclient.data.player

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.vander.core.domain.data.QueuedTrack
import org.vander.core.domain.data.Track
import org.vander.core.domain.player.PlayerStateRepository
import org.vander.core.domain.queue.QueueRepository
import org.vander.core.domain.state.PlayerStateData
import org.vander.core.logger.Logger
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Keeps the playback queue in step with the App Remote, although it comes from the Web API.
 *
 * The two sources are not synchronised: after a skip, the Web API answers with the previous
 * track for a few hundred milliseconds. Two rules follow:
 * - [queue] always starts with the track the App Remote reports, whatever the Web API says, so
 *   the title shown never disagrees with the cover;
 * - a lagging queue is refetched on a track change, at most once per [RETRY_DELAYS] entry,
 *   never on a queue emission: a refetch triggered by the queue it updates would loop.
 *
 * Owned by [SpotifyPlayerController], which runs it in its own scope and republishes [queue].
 */
internal class QueueSynchronizer
    @Inject
    constructor(
        private val queueRepository: QueueRepository,
        private val playerStateRepository: PlayerStateRepository,
        private val logger: Logger,
    ) {
        private val _queue = MutableStateFlow<List<QueuedTrack>>(emptyList())
        val queue: StateFlow<List<QueuedTrack>> = _queue.asStateFlow()

        /** Publishes the queue and refetches it while it lags; suspends until cancelled. */
        suspend fun run() =
            coroutineScope {
                launch { publishQueue() }
                launch { refetchWhileLagging() }
            }

        /** One fetch, outside any lag: the session just became ready. */
        suspend fun refresh() {
            queueRepository
                .refresh()
                .onFailure { logger.e(TAG, "Queue fetch failed", it) }
        }

        private suspend fun publishQueue() {
            combine(queueRepository.currentQueue, playerStateRepository.playerStateData, ::Pair)
                .collect { (queue, snapshot) ->
                    if (queue == null || snapshot.trackId.isEmpty()) return@collect

                    if (queue.currentlyPlaying?.id == snapshot.trackId) {
                        _queue.value = queueOf(snapshot, queue.queue.tracks)
                        return@collect
                    }

                    // Until the Web API catches up, keep the queue's head on the track that is
                    // actually playing: a stale head shows a title that no longer matches the cover.
                    val upcoming = queue.queue.tracks
                    val playingIndex = upcoming.indexOfFirst { it.id == snapshot.trackId }
                    val stillUpcoming = if (playingIndex >= 0) upcoming.drop(playingIndex + 1) else emptyList()
                    _queue.value = queueOf(snapshot, stillUpcoming)
                }
        }

        private suspend fun refetchWhileLagging() {
            playerStateRepository.playerStateData
                .map { it.trackId }
                .filter { it.isNotEmpty() }
                .distinctUntilChanged()
                .collectLatest { trackId ->
                    for (wait in RETRY_DELAYS) {
                        if (isInStepWith(trackId)) return@collectLatest
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
                    if (!isInStepWith(trackId)) {
                        logger.w(TAG, "Queue still lagging behind $trackId, giving up until the next track")
                    }
                }
        }

        private fun isInStepWith(trackId: String): Boolean =
            queueRepository.currentQueue.value
                ?.currentlyPlaying
                ?.id == trackId

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
            const val TAG = "QueueSynchronizer"

            // An immediate refetch almost always comes back stale; see the class comment.
            val RETRY_DELAYS = listOf(500.milliseconds, 1.seconds, 2.seconds)
        }
    }
