package org.vander.spotifyclient.domain.usecase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.vander.core.domain.data.Album
import org.vander.core.domain.data.Artist
import org.vander.core.domain.data.CurrentlyPlaying
import org.vander.core.domain.data.PlaybackContext
import org.vander.core.domain.data.Queue
import org.vander.core.domain.data.QueuedTrack
import org.vander.core.domain.data.SpotifyUri
import org.vander.core.domain.data.Track
import org.vander.core.domain.player.PlayerCommand
import org.vander.core.domain.state.PlayerStateData
import org.vander.core.domain.state.SavedRemotelyChangedState
import org.vander.core.domain.state.SessionState
import org.vander.core.logger.test.FakeLogger
import org.vander.spotifyclient.data.repository.FakePlayerStateRepository
import org.vander.spotifyclient.data.repository.FakeSpotifyPlayerClient
import org.vander.spotifyclient.domain.player.session.FakeSpotifySessionManager
import org.vander.spotifyclient.domain.repository.FakeLibraryRepository
import java.io.IOException

/**
 * The controller is given the test's `backgroundScope`: its collectors run on the test
 * scheduler, so `runCurrent()` settles them deterministically and they are cancelled when the
 * test ends — the same injection that lets production hand it a process-wide scope.
 */
class PlayerUseCaseTest {
    // --- dispatch: transport

    @Test
    fun `Play hands the URI to the client untouched`() =
        runTest {
            val f = Fixture(backgroundScope)
            val uri = SpotifyUri.playlist(PLAYLIST_ID)

            f.controller.dispatch(PlayerCommand.Play(uri))

            assertEquals(uri, f.client.lastPlayed)
            assertEquals("spotify:playlist:$PLAYLIST_ID", f.client.lastPlayed?.value)
        }

    @Test
    fun `Play keeps two kinds built on the same id apart`() =
        runTest {
            val f = Fixture(backgroundScope)

            f.controller.dispatch(PlayerCommand.Play(SpotifyUri.track(SHARED_ID)))
            val asTrack = f.client.lastPlayed
            f.controller.dispatch(PlayerCommand.Play(SpotifyUri.album(SHARED_ID)))

            assertEquals("spotify:track:$SHARED_ID", asTrack?.value)
            assertEquals("spotify:album:$SHARED_ID", f.client.lastPlayed?.value)
        }

    @Test
    fun `transport commands reach the client in order`() =
        runTest {
            val f = Fixture(backgroundScope)

            f.controller.dispatch(PlayerCommand.Pause)
            f.controller.dispatch(PlayerCommand.Resume)
            f.controller.dispatch(PlayerCommand.SkipNext)
            f.controller.dispatch(PlayerCommand.SkipPrevious)
            f.controller.dispatch(PlayerCommand.SeekTo(4_200L))

            assertEquals(listOf("pause", "resume", "skipNext", "skipPrevious", "seekTo:4200"), f.client.commands)
        }

    @Test
    fun `a refusal from the player comes back as a failure`() =
        runTest {
            // What used to end in Logcat: a free account asked to play.
            val f = Fixture(backgroundScope)
            val refusal = IllegalStateException("Premium required")
            f.client.nextResult = Result.failure(refusal)

            val result = f.controller.dispatch(PlayerCommand.Play(SpotifyUri.playlist(PLAYLIST_ID)))

            assertEquals(refusal, result.exceptionOrNull())
        }

    @Test
    fun `TogglePlayPause pauses while playing`() =
        runTest {
            val f = Fixture(backgroundScope)
            f.client.emit(snapshot(trackId = TRACK_ID, isPaused = false))

            f.controller.dispatch(PlayerCommand.TogglePlayPause)

            assertEquals(listOf("pause"), f.client.commands)
        }

    @Test
    fun `TogglePlayPause resumes while paused`() =
        runTest {
            val f = Fixture(backgroundScope)
            f.client.emit(snapshot(trackId = TRACK_ID, isPaused = true))

            f.controller.dispatch(PlayerCommand.TogglePlayPause)

            assertEquals(listOf("resume"), f.client.commands)
        }

    // --- dispatch: ToggleSave, now owned here (review candidate 3)

    @Test
    fun `ToggleSave with no track loaded fails and touches nothing`() =
        runTest {
            val f = Fixture(backgroundScope)

            val result = f.controller.dispatch(PlayerCommand.ToggleSave)

            assertTrue(result.isFailure)
            assertTrue(f.library.saved.isEmpty() && f.library.removed.isEmpty())
        }

    @Test
    fun `ToggleSave persists an unsaved track and publishes the new flag`() =
        runTest {
            val f = Fixture(backgroundScope).started()
            f.stateRepository.emitState(snapshot(trackId = TRACK_ID))
            runCurrent()

            val result = f.controller.dispatch(PlayerCommand.ToggleSave)

            assertTrue(result.isSuccess)
            assertEquals(listOf(TRACK_ID), f.library.saved)
            assertEquals(true, f.controller.state.value.player.isTrackSaved)
        }

    @Test
    fun `ToggleSave removes a saved track`() =
        runTest {
            val f = Fixture(backgroundScope).started()
            f.library.savedIds += TRACK_ID
            f.stateRepository.emitState(snapshot(trackId = TRACK_ID))
            runCurrent()

            f.controller.dispatch(PlayerCommand.ToggleSave)

            assertEquals(listOf(TRACK_ID), f.library.removed)
            assertEquals(false, f.controller.state.value.player.isTrackSaved)
        }

    @Test
    fun `a failed ToggleSave leaves the published flag as it was`() =
        runTest {
            val f = Fixture(backgroundScope).started()
            f.stateRepository.emitState(snapshot(trackId = TRACK_ID))
            runCurrent()
            val before = f.controller.state.value.player.isTrackSaved
            f.library.failWith = IOException("offline")

            val result = f.controller.dispatch(PlayerCommand.ToggleSave)

            assertTrue(result.isFailure)
            assertEquals(before, f.controller.state.value.player.isTrackSaved)
        }

    // --- start

    @Test
    fun `nothing is observed before a session is ready`() =
        runTest {
            val f = Fixture(backgroundScope).started()
            runCurrent()

            assertEquals(0, f.stateRepository.startListeningCount)
            assertEquals(0, f.remote.fetchCount)
        }

    @Test
    fun `a ready session starts listening and fetches the queue`() =
        runTest {
            val f = Fixture(backgroundScope).started()

            f.session.emit(SessionState.Ready)
            runCurrent()

            assertEquals(1, f.stateRepository.startListeningCount)
            assertEquals(1, f.remote.fetchCount)
        }

    @Test
    fun `start is idempotent`() =
        runTest {
            // Two calls used to launch the collectors twice. Any screen may now call start().
            val f = Fixture(backgroundScope)
            f.controller.start()
            f.controller.start()
            runCurrent()

            f.session.emit(SessionState.Ready)
            runCurrent()

            assertEquals(1, f.stateRepository.startListeningCount)
        }

    // --- published state

    @Test
    fun `a player snapshot is published without waiting for the queue`() =
        runTest {
            val f = Fixture(backgroundScope).started()

            f.stateRepository.emitState(snapshot(trackId = TRACK_ID, trackName = "Nuits blanches"))
            runCurrent()

            assertEquals("Nuits blanches", f.controller.state.value.player.base.trackName)
        }

    @Test
    fun `the saved flag is looked up once per track, not once per tick`() =
        runTest {
            // The previous implementation queried the Web API on every position update.
            val f = Fixture(backgroundScope).started()

            f.stateRepository.emitState(snapshot(trackId = TRACK_ID, positionMs = 1_000))
            runCurrent()
            f.stateRepository.emitState(snapshot(trackId = TRACK_ID, positionMs = 2_000))
            runCurrent()
            f.stateRepository.emitState(snapshot(trackId = TRACK_ID, positionMs = 3_000))
            runCurrent()

            assertEquals(listOf(TRACK_ID), f.library.lookups)
        }

    @Test
    fun `the saved flag lands in the published state`() =
        runTest {
            val f = Fixture(backgroundScope).started()
            f.library.savedIds += TRACK_ID

            f.stateRepository.emitState(snapshot(trackId = TRACK_ID))
            runCurrent()

            assertEquals(true, f.controller.state.value.player.isTrackSaved)
        }

    @Test
    fun `a track change forgets the previous track's flag`() =
        runTest {
            val f = Fixture(backgroundScope).started()
            f.library.savedIds += TRACK_ID
            f.stateRepository.emitState(snapshot(trackId = TRACK_ID))
            runCurrent()
            f.library.failWith = IOException("offline")

            f.stateRepository.emitState(snapshot(trackId = "another"))
            runCurrent()

            // Unknown, not the previous track's `true`.
            assertNull(f.controller.state.value.player.isTrackSaved)
        }

    @Test
    fun `a save from another device triggers a new lookup`() =
        runTest {
            val f = Fixture(backgroundScope).started()
            f.stateRepository.emitState(snapshot(trackId = TRACK_ID))
            runCurrent()
            f.library.savedIds += TRACK_ID

            f.stateRepository.emitSavedRemotely(SavedRemotelyChangedState(isSaved = true, trackId = TRACK_ID))
            runCurrent()

            assertEquals(true, f.controller.state.value.player.isTrackSaved)
        }

    @Test
    fun `the queue is published with the current track first`() =
        runTest {
            val f = Fixture(backgroundScope).started()
            f.remote.nextQueue = queue(current = TRACK_ID, next = listOf("n1", "n2"))
            f.session.emit(SessionState.Ready)
            runCurrent()

            f.stateRepository.emitState(snapshot(trackId = TRACK_ID, trackName = "Now", artistName = "Elia Faure"))
            runCurrent()

            val queue = f.controller.state.value.queue
            assertEquals(listOf(TRACK_ID, "n1", "n2"), queue.map { it.id })
            assertEquals(QueuedTrack(TRACK_ID, "Now", "Elia Faure"), queue.first())
        }

    @Test
    fun `a hole in the queue does not crash the collector`() =
        runTest {
            // Regression: `artists[0]` threw on Track.empty(), the stand-in for a null slot,
            // and the exception killed the collector for the rest of the session.
            val f = Fixture(backgroundScope).started()
            f.remote.nextQueue =
                CurrentlyPlaying(
                    currentlyPlaying = track(TRACK_ID),
                    queue = Queue(listOf(track("n1"), Track.empty())),
                )
            f.session.emit(SessionState.Ready)
            runCurrent()

            f.stateRepository.emitState(snapshot(trackId = TRACK_ID))
            runCurrent()

            val queue = f.controller.state.value.queue
            assertEquals(listOf(TRACK_ID, "n1", ""), queue.map { it.id })
            assertEquals("", queue.last().artistName)
        }

    @Test
    fun `a queue out of step with the player is refetched once, not in a loop`() =
        runTest {
            // The Web API lags behind the App Remote after a skip. Refetching on every
            // mismatch re-emitted the queue, which triggered another mismatch, and so on.
            val f = Fixture(backgroundScope).started()
            f.remote.nextQueue = queue(current = "stale", next = listOf("n1"))
            f.session.emit(SessionState.Ready)
            runCurrent()

            f.stateRepository.emitState(snapshot(trackId = TRACK_ID))
            runCurrent()
            f.stateRepository.emitState(snapshot(trackId = TRACK_ID, positionMs = 2_000))
            runCurrent()

            // One fetch on Ready, one refetch for the mismatch — and no more.
            assertEquals(2, f.remote.fetchCount)
            assertTrue(f.controller.state.value.queue.isEmpty())
        }

    @Test
    fun `the playback context is republished into the state`() =
        runTest {
            val f = Fixture(backgroundScope).started()
            val context = PlaybackContext(uri = SpotifyUri.playlist(PLAYLIST_ID), title = "Sillons")

            f.stateRepository.emitContext(context)
            runCurrent()

            assertEquals(PLAYLIST_ID, f.controller.state.value.context.playlistId)
        }

    // --- fixtures

    private class Fixture(
        scope: CoroutineScope,
    ) {
        val session = FakeSpotifySessionManager()
        val remote = FakeSpotifyRemoteUseCase()
        val stateRepository = FakePlayerStateRepository()
        val library = FakeLibraryRepository()
        val client = FakeSpotifyPlayerClient()

        val controller =
            PlayerUseCase(
                sessionManager = session,
                remoteUseCase = remote,
                playerStateRepository = stateRepository,
                libraryRepository = library,
                playerClient = client,
                scope = scope,
                logger = FakeLogger(),
            )
    }

    /** Starts the controller; the caller settles it with `runCurrent()` once it has emitted. */
    private fun Fixture.started(): Fixture = apply { controller.start() }

    private fun snapshot(
        trackId: String,
        trackName: String = "Track",
        artistName: String = "Artist",
        isPaused: Boolean = false,
        positionMs: Long = 0,
    ) = PlayerStateData.empty().copy(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        isPaused = isPaused,
        paused = isPaused,
        playing = !isPaused,
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
            artists = listOf(Artist(externalUrls = "", href = "", id = "ar", name = "Artist of $id", type = "artist", uri = "")),
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

        const val PLAYLIST_ID = "37i9dQZF1DXcBWIGoYBM5M"

        const val SHARED_ID = "1A2b3C4d5E6f7G8h9I0jKl"
    }
}
