package org.vander.spotifyclient.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.vander.core.domain.data.CurrentlyPlaying
import org.vander.core.domain.data.Queue
import org.vander.core.domain.data.Track
import org.vander.core.logger.test.FakeLogger
import org.vander.spotifyclient.domain.repository.SpotifyQueueRepository
import java.io.IOException

class SpotifyRemoteUseCaseImplTest {
    @Test
    fun `the queue starts null`() =
        runTest {
            assertNull(SpotifyRemoteUseCaseImpl(FakeQueueRepository(), FakeLogger()).currentUserQueue.value)
        }

    @Test
    fun `a successful fetch publishes the queue`() =
        runTest {
            val useCase = SpotifyRemoteUseCaseImpl(FakeQueueRepository(Result.success(queueOf("a", "b"))), FakeLogger())

            useCase.getAndEmitUserQueueFlow()

            assertEquals(
                listOf("a", "b"),
                useCase.currentUserQueue.value
                    ?.queue
                    ?.tracks
                    ?.map { it.id },
            )
        }

    @Test
    fun `the flow emits once per successful fetch`() =
        runTest {
            val useCase = SpotifyRemoteUseCaseImpl(FakeQueueRepository(Result.success(queueOf("a"))), FakeLogger())

            useCase.currentUserQueue.test {
                assertNull(awaitItem())

                useCase.getAndEmitUserQueueFlow()

                assertNotNull(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `a failure keeps the previous queue instead of clearing it`() =
        runTest {
            // This is what separates it from PlaylistUseCase, which collapses to empty:
            // an intermittent network error leaves the displayed queue in place.
            val repository = FakeQueueRepository(Result.success(queueOf("a", "b")))
            val useCase = SpotifyRemoteUseCaseImpl(repository, FakeLogger())

            useCase.getAndEmitUserQueueFlow()
            repository.nextResult = Result.failure(IOException("offline"))
            useCase.getAndEmitUserQueueFlow()

            assertEquals(
                listOf("a", "b"),
                useCase.currentUserQueue.value
                    ?.queue
                    ?.tracks
                    ?.map { it.id },
            )
        }

    @Test
    fun `a failure is logged with its exception`() =
        runTest {
            val boom = IOException("offline")
            val logger = FakeLogger()

            SpotifyRemoteUseCaseImpl(FakeQueueRepository(Result.failure(boom)), logger).getAndEmitUserQueueFlow()

            assertEquals(FakeLogger.Entry.Level.ERROR, logger.last()?.level)
            assertEquals(boom, logger.last()?.throwable)
        }

    private fun queueOf(vararg ids: String) =
        CurrentlyPlaying(
            currentlyPlaying = null,
            queue = Queue(ids.map { trackWithId(it) }),
        )

    private fun trackWithId(id: String) =
        Track(
            album = Track.empty().album,
            artists = emptyList(),
            availableMarkets = emptyList(),
            discNumber = 0,
            durationMs = 0,
            explicit = false,
            externalIds = "",
            externalUrls = "",
            href = "",
            id = id,
            isPlayable = true,
            name = "",
            trackNumber = 0,
            type = "track",
            uri = "spotify:track:$id",
        )

    private class FakeQueueRepository(
        var nextResult: Result<CurrentlyPlaying> = Result.success(CurrentlyPlaying.empty()),
    ) : SpotifyQueueRepository {
        private val _currentQueue = MutableStateFlow<CurrentlyPlaying?>(null)
        override val currentQueue: StateFlow<CurrentlyPlaying?> = _currentQueue

        override suspend fun getUserQueue(): Result<CurrentlyPlaying> = nextResult
    }
}
