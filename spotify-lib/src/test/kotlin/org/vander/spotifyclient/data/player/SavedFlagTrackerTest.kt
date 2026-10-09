package org.vander.spotifyclient.data.player

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.vander.core.domain.state.PlayerStateData
import org.vander.core.domain.state.SavedRemotelyChangedState
import org.vander.core.logger.test.FakeLogger
import org.vander.spotifyclient.data.repository.FakePlayerStateRepository
import org.vander.spotifyclient.domain.repository.FakeLibraryRepository
import java.io.IOException

/** Runs the tracker in the test's `backgroundScope`, as [SpotifyPlayerController] does in its own. */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SavedFlagTrackerTest {
    @Test
    fun `the flag is looked up once per track, not once per tick`() =
        runTest {
            // The previous implementation queried the Web API on every position update.
            val f = Fixture(backgroundScope)

            f.stateRepository.emitState(snapshot(trackId = TRACK_ID, positionMs = 1_000))
            runCurrent()
            f.stateRepository.emitState(snapshot(trackId = TRACK_ID, positionMs = 2_000))
            runCurrent()
            f.stateRepository.emitState(snapshot(trackId = TRACK_ID, positionMs = 3_000))
            runCurrent()

            assertEquals(listOf(TRACK_ID), f.library.lookups)
        }

    @Test
    fun `a save from another device triggers a new lookup`() =
        runTest {
            val f = Fixture(backgroundScope)
            f.stateRepository.emitState(snapshot(trackId = TRACK_ID))
            runCurrent()
            f.library.savedIds += TRACK_ID

            f.stateRepository.emitSavedRemotely(SavedRemotelyChangedState(isSaved = true, trackId = TRACK_ID))
            runCurrent()

            assertEquals(SavedFlag(TRACK_ID, isSaved = true), f.tracker.flag.value)
        }

    @Test
    fun `toggle with no track loaded fails and touches nothing`() =
        runTest {
            val f = Fixture(backgroundScope)

            val result = f.tracker.toggle()

            assertTrue(result.isFailure)
            assertTrue(f.library.saved.isEmpty() && f.library.removed.isEmpty())
        }

    @Test
    fun `toggle removes a saved track`() =
        runTest {
            val f = Fixture(backgroundScope)
            f.library.savedIds += TRACK_ID
            f.stateRepository.emitState(snapshot(trackId = TRACK_ID))
            runCurrent()

            f.tracker.toggle()

            assertEquals(listOf(TRACK_ID), f.library.removed)
            assertEquals(SavedFlag(TRACK_ID, isSaved = false), f.tracker.flag.value)
        }

    @Test
    fun `a failed toggle leaves the flag as it was`() =
        runTest {
            val f = Fixture(backgroundScope)
            f.stateRepository.emitState(snapshot(trackId = TRACK_ID))
            runCurrent()
            val before = f.tracker.flag.value
            f.library.failWith = IOException("offline")

            val result = f.tracker.toggle()

            assertTrue(result.isFailure)
            assertEquals(before, f.tracker.flag.value)
        }

    // --- fixtures

    private class Fixture(
        scope: CoroutineScope,
    ) {
        val stateRepository = FakePlayerStateRepository()
        val library = FakeLibraryRepository()
        val tracker = SavedFlagTracker(library, stateRepository, FakeLogger())

        init {
            scope.launch { tracker.run() }
        }
    }

    private fun snapshot(
        trackId: String,
        positionMs: Long = 0,
    ) = PlayerStateData.empty().copy(
        trackId = trackId,
        isPaused = false,
        paused = false,
        playing = true,
        positionMs = positionMs,
        durationMs = 214_000,
    )

    private companion object {
        const val TRACK_ID = "4cOdK2wGLETKBW3PvgPWqT"
    }
}
