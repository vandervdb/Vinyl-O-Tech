package org.vander.spotifyclient.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.vander.core.domain.data.Playlist
import org.vander.core.domain.data.PlaylistCollection
import org.vander.core.logger.test.FakeLogger
import org.vander.spotifyclient.domain.repository.SpotifyPlaylistRepository
import java.io.IOException

class PlaylistUseCaseTest {
    @Test
    fun `playlists starts empty rather than null`() =
        runTest {
            val useCase = PlaylistUseCase(FakePlaylistRepository(), FakeLogger())

            assertTrue(
                useCase.playlists.value.items
                    .isEmpty(),
            )
        }

    @Test
    fun `a successful load publishes the collection`() =
        runTest {
            val collection = PlaylistCollection(listOf(playlist("a", "Sillons"), playlist("b", "Braise")))
            val useCase = PlaylistUseCase(FakePlaylistRepository(Result.success(collection)), FakeLogger())

            useCase.getAndUpdatePlaylistsFlow()

            assertEquals(
                listOf("Sillons", "Braise"),
                useCase.playlists.value.items
                    .map { it.name },
            )
        }

    @Test
    fun `the flow emits once per successful load`() =
        runTest {
            val useCase =
                PlaylistUseCase(
                    FakePlaylistRepository(Result.success(PlaylistCollection(listOf(playlist("a", "Sillons"))))),
                    FakeLogger(),
                )

            useCase.playlists.test {
                assertTrue(awaitItem().items.isEmpty())

                useCase.getAndUpdatePlaylistsFlow()

                assertEquals("Sillons", awaitItem().items.single().name)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `a failure collapses to an empty collection and is logged`() =
        runTest {
            // Documented trade-off: the caller cannot tell "no playlists" from "the call
            // failed" — the grid shows nothing either way.
            val logger = FakeLogger()
            val useCase = PlaylistUseCase(FakePlaylistRepository(Result.failure(IOException("offline"))), logger)

            useCase.getAndUpdatePlaylistsFlow()

            assertTrue(
                useCase.playlists.value.items
                    .isEmpty(),
            )
            assertTrue(logger.contains(FakeLogger.Entry.Level.ERROR, "PlaylistUseCase", "Error getting user playlists"))
        }

    @Test
    fun `a failure after a success clears what was published`() =
        runTest {
            val repository =
                FakePlaylistRepository(Result.success(PlaylistCollection(listOf(playlist("a", "Sillons")))))
            val useCase = PlaylistUseCase(repository, FakeLogger())

            useCase.getAndUpdatePlaylistsFlow()
            repository.nextResult = Result.failure(IOException("offline"))
            useCase.getAndUpdatePlaylistsFlow()

            assertTrue(
                useCase.playlists.value.items
                    .isEmpty(),
            )
        }

    @Test
    fun `the exception is passed to the logger, not just its message`() =
        runTest {
            val boom = IOException("offline")
            val logger = FakeLogger()

            PlaylistUseCase(FakePlaylistRepository(Result.failure(boom)), logger).getAndUpdatePlaylistsFlow()

            assertEquals(boom, logger.last()?.throwable)
        }

    private fun playlist(
        id: String,
        name: String,
    ) = Playlist(id = id, name = name, coverUrl = "")

    /**
     * Hand-written rather than a MockK stub: the test needs to change the answer between two
     * calls, which reads better as a mutable field than as `returnsMany`.
     */
    private class FakePlaylistRepository(
        var nextResult: Result<PlaylistCollection> = Result.success(PlaylistCollection.empty()),
    ) : SpotifyPlaylistRepository {
        private val _playlists = MutableStateFlow<PlaylistCollection?>(null)
        override val playlists: StateFlow<PlaylistCollection?> = _playlists

        override suspend fun getUserPlaylists(): Result<PlaylistCollection> = nextResult
    }
}
