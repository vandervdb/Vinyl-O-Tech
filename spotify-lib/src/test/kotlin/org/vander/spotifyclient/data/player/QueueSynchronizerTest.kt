package org.vander.spotifyclient.data.player

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.vander.core.domain.data.Album
import org.vander.core.domain.data.Artist
import org.vander.core.domain.data.CurrentlyPlaying
import org.vander.core.domain.data.Queue
import org.vander.core.domain.data.QueuedTrack
import org.vander.core.domain.data.Track
import org.vander.core.domain.state.PlayerStateData
import org.vander.core.logger.test.FakeLogger
import org.vander.spotifyclient.data.repository.FakePlayerStateRepository
import org.vander.spotifyclient.domain.repository.FakeQueueRepository
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Runs the synchronizer in the test's `backgroundScope`, as [SpotifyPlayerController] does in its
 * own: `runCurrent()` settles it, `advanceTimeBy` drives the retry delays on virtual time.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class QueueSynchronizerTest {
    @Test
    fun `the queue is published with the current track first`() =
        runTest {
            val f = Fixture(backgroundScope)
            f.queueRepository.nextQueue = queue(current = TRACK_ID, next = listOf("n1", "n2"))
            f.synchronizer.refresh()
            runCurrent()

            f.stateRepository.emitState(snapshot(trackId = TRACK_ID, trackName = "Now", artistName = "Elia Faure"))
            runCurrent()

            val queue = f.synchronizer.queue.value
            assertEquals(listOf(TRACK_ID, "n1", "n2"), queue.map { it.id })
            assertEquals(QueuedTrack(TRACK_ID, "Now", "Elia Faure"), queue.first())
        }

    @Test
    fun `a hole in the queue does not crash the collector`() =
        runTest {
            // Regression: `artists[0]` threw on Track.empty(), the stand-in for a null slot,
            // and the exception killed the collector for the rest of the session.
            val f = Fixture(backgroundScope)
            f.queueRepository.nextQueue =
                CurrentlyPlaying(
                    currentlyPlaying = track(TRACK_ID),
                    queue = Queue(listOf(track("n1"), Track.empty())),
                )
            f.synchronizer.refresh()
            runCurrent()

            f.stateRepository.emitState(snapshot(trackId = TRACK_ID))
            runCurrent()

            val queue = f.synchronizer.queue.value
            assertEquals(listOf(TRACK_ID, "n1", ""), queue.map { it.id })
            assertEquals("", queue.last().artistName)
        }

    @Test
    fun `a lagging queue still shows the playing track first`() =
        runTest {
            // The Web API still reports the previous track: the head must follow the App Remote,
            // like the cover does, or the title and the cover disagree.
            val f = Fixture(backgroundScope)
            f.queueRepository.nextQueue = queue(current = "stale", next = listOf("n1"))
            f.synchronizer.refresh()
            runCurrent()

            f.stateRepository.emitState(snapshot(trackId = TRACK_ID))
            runCurrent()

            assertEquals(
                TRACK_ID,
                f.synchronizer.queue.value
                    .first()
                    .id,
            )
        }

    @Test
    fun `a lagging queue keeps what still comes after the playing track`() =
        runTest {
            // A skip within the same album: the stale queue already holds the rest.
            val f = Fixture(backgroundScope)
            f.queueRepository.nextQueue = queue(current = "stale", next = listOf("n1", TRACK_ID, "n2"))
            f.synchronizer.refresh()
            runCurrent()

            f.stateRepository.emitState(snapshot(trackId = TRACK_ID))
            runCurrent()

            assertEquals(
                listOf(TRACK_ID, "n2"),
                f.synchronizer.queue.value
                    .map { it.id },
            )
        }

    @Test
    fun `a queue that never catches up is refetched a bounded number of times, not in a loop`() =
        runTest {
            // The Web API lags behind the App Remote after a skip. Refetching on every
            // mismatch re-emitted the queue, which triggered another mismatch, and so on.
            val f = Fixture(backgroundScope)
            f.queueRepository.nextQueue = queue(current = "stale", next = listOf("n1"))
            f.synchronizer.refresh()
            runCurrent()

            f.stateRepository.emitState(snapshot(trackId = TRACK_ID))
            runCurrent()
            f.stateRepository.emitState(snapshot(trackId = TRACK_ID, positionMs = 2_000))
            advanceTimeBy(10.seconds)
            runCurrent()

            // The initial fetch, then one per retry delay — and no more.
            assertEquals(1 + 3, f.queueRepository.refreshCount)
            // The playing track is not in the stale queue: only it can be shown.
            assertEquals(
                listOf(TRACK_ID),
                f.synchronizer.queue.value
                    .map { it.id },
            )
        }

    @Test
    fun `a failed refetch does not kill the queue collector`() =
        runTest {
            val f = Fixture(backgroundScope)
            f.queueRepository.nextQueue = queue(current = "stale", next = listOf("n1"))
            f.synchronizer.refresh()
            runCurrent()

            f.queueRepository.nextQueue = null
            f.stateRepository.emitState(snapshot(trackId = TRACK_ID))
            advanceTimeBy(500.milliseconds)
            runCurrent()

            f.queueRepository.nextQueue = queue(current = "other", next = listOf("n2"))
            f.stateRepository.emitState(snapshot(trackId = "other"))
            advanceTimeBy(500.milliseconds)
            runCurrent()

            // Initial fetch + failed refetch + refetch for the new track: the collector survived.
            assertEquals(3, f.queueRepository.refreshCount)
        }

    // --- fixtures

    private class Fixture(
        scope: CoroutineScope,
    ) {
        val queueRepository = FakeQueueRepository()
        val stateRepository = FakePlayerStateRepository()
        val synchronizer = QueueSynchronizer(queueRepository, stateRepository, FakeLogger())

        init {
            scope.launch { synchronizer.run() }
        }
    }

    private fun snapshot(
        trackId: String,
        trackName: String = "Track",
        artistName: String = "Artist",
        positionMs: Long = 0,
    ) = PlayerStateData.empty().copy(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        isPaused = false,
        paused = false,
        playing = true,
        positionMs = positionMs,
        durationMs = 214_000,
    )

    private fun queue(
        current: String,
        next: List<String>,
    ) = CurrentlyPlaying(
        currentlyPlaying = track(current),
        queue = Queue(next.map { track(it) }),
    )

    private fun track(id: String) =
        Track(
            album = Album.empty(),
            artists =
                listOf(
                    Artist(externalUrls = "", href = "", id = "ar", name = "Artist of $id", type = "artist", uri = ""),
                ),
            availableMarkets = emptyList(),
            discNumber = 1,
            durationMs = 214_000,
            explicit = false,
            externalIds = "",
            externalUrls = "",
            href = "",
            id = id,
            isPlayable = true,
            name = "Name of $id",
            trackNumber = 1,
            type = "track",
            uri = "spotify:track:$id",
        )

    private companion object {
        const val TRACK_ID = "4cOdK2wGLETKBW3PvgPWqT"
    }
}
