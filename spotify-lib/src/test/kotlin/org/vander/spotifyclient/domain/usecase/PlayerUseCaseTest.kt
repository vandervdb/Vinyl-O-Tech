package org.vander.spotifyclient.domain.usecase

import app.cash.turbine.test
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.vander.core.domain.data.PlaybackContext
import org.vander.core.domain.data.SpotifyUri
import org.vander.spotifyclient.data.repository.FakePlayerStateRepository
import org.vander.spotifyclient.data.repository.FakeSpotifyPlayerClient
import org.vander.spotifyclient.domain.player.PlayerClient
import org.vander.spotifyclient.domain.player.session.FakeSpotifySessionManager
import org.vander.spotifyclient.domain.repository.FakeLibraryRepository

/**
 * Covers the commands only. [PlayerUseCase.init] launches three collectors on flows that
 * never complete, so a test that called it would never return — the state side needs the
 * collectors to be startable one at a time before it can be covered here.
 */
class PlayerUseCaseTest {
    @Test
    fun `play hands the playlist URI to the client untouched`() =
        runTest {
            val client = FakeSpotifyPlayerClient()
            val useCase = useCaseWith(client)
            val uri = SpotifyUri.playlist(PLAYLIST_ID)

            useCase.play(uri)

            assertEquals(uri, client.lastPlayed)
            assertEquals("spotify:playlist:$PLAYLIST_ID", client.lastPlayed?.value)
        }

    @Test
    fun `play does not re-prefix a track URI`() =
        runTest {
            // The regression this guards: the use case used to build "spotify:track:$id"
            // itself, so anything already prefixed came out doubled.
            val client = FakeSpotifyPlayerClient()
            val useCase = useCaseWith(client)

            useCase.play(SpotifyUri.track(TRACK_ID))

            assertEquals("spotify:track:$TRACK_ID", client.lastPlayed?.value)
        }

    @Test
    fun `play keeps two kinds built on the same id apart`() =
        runTest {
            val client = FakeSpotifyPlayerClient()
            val useCase = useCaseWith(client)

            useCase.play(SpotifyUri.track(SHARED_ID))
            val asTrack = client.lastPlayed
            useCase.play(SpotifyUri.album(SHARED_ID))

            assertEquals("spotify:track:$SHARED_ID", asTrack?.value)
            assertEquals("spotify:album:$SHARED_ID", client.lastPlayed?.value)
        }

    @Test
    fun `transport commands are forwarded to the client`() =
        runTest {
            // MockK here rather than the fake: these take no value-class argument, so the
            // JVM signature is the Kotlin one and verification is exact.
            val client = mockk<PlayerClient>(relaxed = true)
            val useCase = useCaseWith(client)

            useCase.pause()
            useCase.resume()
            useCase.skipNext()
            useCase.skipPrevious()
            useCase.seekTo(4_200L)

            coVerify(exactly = 1) { client.pause() }
            coVerify(exactly = 1) { client.resume() }
            coVerify(exactly = 1) { client.skipNext() }
            coVerify(exactly = 1) { client.skipPrevious() }
            coVerify(exactly = 1) { client.seekTo(4_200L) }
        }

    @Test
    fun `playbackContext is republished from the repository`() =
        runTest {
            // The use case adds nothing here: unlike the player state and the queue, the
            // context needs no merging, so it is the repository's flow itself.
            val stateRepository = FakePlayerStateRepository()
            val useCase = useCaseWith(FakeSpotifyPlayerClient(), stateRepository)
            val context = PlaybackContext(uri = SpotifyUri.playlist(PLAYLIST_ID), title = "Sillons")

            useCase.playbackContext.test {
                assertEquals(PlaybackContext.None, awaitItem())

                stateRepository.emitContext(context)

                assertEquals(PLAYLIST_ID, awaitItem().playlistId)
                cancelAndIgnoreRemainingEvents()
            }
        }

    /**
     * `playerStateRepository` and `playerRepository` are two constructor parameters bound to
     * the same instance in production; the test mirrors that rather than passing two, so a
     * behaviour depending on their identity is not hidden.
     */
    private fun useCaseWith(
        playerClient: PlayerClient,
        stateRepository: FakePlayerStateRepository = FakePlayerStateRepository(),
    ): PlayerUseCase =
        PlayerUseCase(
            sessionUseCase = FakeSpotifySessionManager(),
            remoteUseCase = FakeSpotifyRemoteUseCase(),
            playerStateRepository = stateRepository,
            libraryRepository = FakeLibraryRepository(),
            playerRepository = stateRepository,
            playerClient = playerClient,
        )

    private companion object {
        const val TRACK_ID = "4cOdK2wGLETKBW3PvgPWqT"

        const val PLAYLIST_ID = "37i9dQZF1DXcBWIGoYBM5M"

        const val SHARED_ID = "1A2b3C4d5E6f7G8h9I0jKl"
    }
}
