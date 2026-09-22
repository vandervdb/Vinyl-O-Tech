package org.vander.spotifyclient.domain.usecase

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.vander.core.domain.data.Album
import org.vander.core.domain.data.Artist
import org.vander.core.domain.data.RecentPlay
import org.vander.core.domain.data.RecentlyPlayed
import org.vander.core.domain.data.SpotifyUri
import org.vander.core.domain.data.Track
import org.vander.core.dto.PlayContextDto
import org.vander.core.dto.PlayHistoryDto
import org.vander.core.dto.RecentlyPlayedResponseDto
import org.vander.core.logger.test.FakeLogger
import org.vander.spotifyclient.data.repository.SpotifyRecentlyPlayedRepositoryImpl
import org.vander.spotifyclient.domain.datasource.IRemoteRecentlyPlayedDataSource
import org.vander.spotifyclient.domain.repository.RecentlyPlayedRepository
import org.vander.spotifyclient.fixtures.trackDto
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class RecentlyPlayedRepositoryTest {
    @Test
    fun `recentlyPlayed state starts empty rather then null`() =
        runTest {
            val useCase = RecentlyPlayedUseCase(FakeRecentlyPlayedRepository(), FakeLogger())
            assertTrue(
                useCase.recentlyPlayed.value.items
                    .isEmpty(),
            )
        }

    @Test
    fun `a successful load publishes the collection`() =
        runTest {
            val results =
                RecentlyPlayed(
                    listOf(
                        RecentPlay(
                            track = track("a"),
                            playedAt = "2022-01-01T00:00:00Z",
                            null,
                        ),
                        RecentPlay(
                            track = track("b"),
                            playedAt = "2022-01-01T00:00:00Z",
                            null,
                        ),
                    ),
                )
            val useCase =
                RecentlyPlayedUseCase(FakeRecentlyPlayedRepository(nextResults = Result.success(results)), FakeLogger())
            useCase.getAndUpdateRecentlyPlayedFlow()
            assertTrue {
                useCase.recentlyPlayed.value.items
                    .map { it.track.id }
                    .containsAll(listOf("a", "b"))
            }
        }

    @Test
    fun `the flow emits once per successful load`() =
        runTest {
            val results = recentlyPlayed(listOf("a", "b", "c"))
            val useCase =
                RecentlyPlayedUseCase(FakeRecentlyPlayedRepository(nextResults = Result.success(results)), FakeLogger())

            useCase.recentlyPlayed.test {
                assertTrue(awaitItem().items.isEmpty())
                useCase.getAndUpdateRecentlyPlayedFlow()
                assertEquals(listOf("a", "b", "c"), awaitItem().items.map { it.track.id })
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `a failure collapses to an empty collection and is logged`() =
        runTest {
            val logger = FakeLogger()
            val useCase =
                RecentlyPlayedUseCase(
                    FakeRecentlyPlayedRepository(nextResults = Result.failure(IOException("offline"))),
                    logger,
                )
            useCase.getAndUpdateRecentlyPlayedFlow()
            assertTrue(
                useCase.recentlyPlayed.value.items.isEmpty(),
            )
            assertTrue(
                logger.contains(
                    FakeLogger.Entry.Level.ERROR,
                    "RecentlyPlayedUseCase",
                    "Error occurred while fetching user recently played",
                ),
            )
        }

    @Test
    fun `a second successful load replaces the first one`() =
        runTest {
            val repository =
                FakeRecentlyPlayedRepository(nextResults = Result.success(recentlyPlayed(listOf("a", "b"))))
            val useCase = RecentlyPlayedUseCase(repository, FakeLogger())

            useCase.getAndUpdateRecentlyPlayedFlow()
            repository.nextResults = Result.success(recentlyPlayed(listOf("c")))
            useCase.getAndUpdateRecentlyPlayedFlow()

            assertEquals(listOf("c"), useCase.recentlyPlayed.value.items.map { it.track.id })
        }

    @Test
    fun `a failure after a success keeps the last published collection`() =
        runTest {
            // Unlike the profile, the history is not reset on failure: offline, the screen
            // keeps showing what it last had instead of going blank.
            val repository =
                FakeRecentlyPlayedRepository(nextResults = Result.success(recentlyPlayed(listOf("a", "b"))))
            val useCase = RecentlyPlayedUseCase(repository, FakeLogger())

            useCase.getAndUpdateRecentlyPlayedFlow()
            repository.nextResults = Result.failure(IOException("offline"))
            useCase.getAndUpdateRecentlyPlayedFlow()

            assertEquals(listOf("a", "b"), useCase.recentlyPlayed.value.items.map { it.track.id })
        }

    @Test
    fun `a failure emits nothing on the flow`() =
        runTest {
            val useCase =
                RecentlyPlayedUseCase(
                    FakeRecentlyPlayedRepository(nextResults = Result.failure(IOException("offline"))),
                    FakeLogger(),
                )

            useCase.recentlyPlayed.test {
                assertTrue(awaitItem().items.isEmpty())
                useCase.getAndUpdateRecentlyPlayedFlow()
                expectNoEvents()
            }
        }

    @Test
    fun `a failure is logged with its cause`() =
        runTest {
            val logger = FakeLogger()
            val cause = IOException("offline")
            val useCase =
                RecentlyPlayedUseCase(FakeRecentlyPlayedRepository(nextResults = Result.failure(cause)), logger)

            useCase.getAndUpdateRecentlyPlayedFlow()

            assertSame(cause, logger.last()?.throwable)
        }

    @Test
    fun `a success is not logged as an error`() =
        runTest {
            val logger = FakeLogger()
            val useCase =
                RecentlyPlayedUseCase(
                    FakeRecentlyPlayedRepository(nextResults = Result.success(recentlyPlayed(listOf("a")))),
                    logger,
                )

            useCase.getAndUpdateRecentlyPlayedFlow()

            assertEquals(0, logger.count(FakeLogger.Entry.Level.ERROR))
        }

    @Test
    fun `lastResumable of the published history skips plays without context`() =
        runTest {
            val history =
                RecentlyPlayed(
                    listOf(
                        RecentPlay(track = track("alone"), playedAt = "2022-01-01T00:00:00Z", contextUri = null),
                        RecentPlay(
                            track = track("fromPlaylist"),
                            playedAt = "2022-01-01T00:00:00Z",
                            contextUri = SpotifyUri.playlist("p1"),
                        ),
                    ),
                )
            val useCase =
                RecentlyPlayedUseCase(FakeRecentlyPlayedRepository(nextResults = Result.success(history)), FakeLogger())

            useCase.getAndUpdateRecentlyPlayedFlow()

            assertEquals("fromPlaylist", useCase.recentlyPlayed.value.lastResumable?.track?.id)
        }

    @Test
    fun `lastResumable is null when no play has a context`() =
        runTest {
            val history =
                RecentlyPlayed(
                    listOf(
                        RecentPlay(track = track("a"), playedAt = "2022-01-01T00:00:00Z", contextUri = null),
                        RecentPlay(track = track("b"), playedAt = "2022-01-01T00:00:00Z", contextUri = null),
                    ),
                )
            val useCase =
                RecentlyPlayedUseCase(FakeRecentlyPlayedRepository(nextResults = Result.success(history)), FakeLogger())

            useCase.getAndUpdateRecentlyPlayedFlow()

            assertNull(useCase.recentlyPlayed.value.lastResumable)
        }

    @Test
    fun `a successful load is logged at debug level`() =
        runTest {
            val logger = FakeLogger()
            val useCase =
                RecentlyPlayedUseCase(
                    FakeRecentlyPlayedRepository(nextResults = Result.success(recentlyPlayed(listOf("a")))),
                    logger,
                )

            useCase.getAndUpdateRecentlyPlayedFlow()

            assertEquals(1, logger.count(FakeLogger.Entry.Level.DEBUG))
            assertTrue(
                logger.contains(
                    FakeLogger.Entry.Level.DEBUG,
                    "RecentlyPlayedUseCase",
                    "Received user recently played",
                ),
            )
        }

    @Test
    fun `a failure is not logged at debug level`() =
        runTest {
            val logger = FakeLogger()
            val useCase =
                RecentlyPlayedUseCase(
                    FakeRecentlyPlayedRepository(nextResults = Result.failure(IOException("offline"))),
                    logger,
                )

            useCase.getAndUpdateRecentlyPlayedFlow()

            assertEquals(0, logger.count(FakeLogger.Entry.Level.DEBUG))
        }

    // --- SpotifyRecentlyPlayedRepositoryImpl

    @Test
    fun `the repository starts with nothing published`() {
        val repository =
            SpotifyRecentlyPlayedRepositoryImpl(dataSourceReturning(Result.success(RecentlyPlayedResponseDto())))

        assertNull(repository.recentlyPlayed.value)
    }

    @Test
    fun `the repository maps the page and returns it`() =
        runTest {
            val repository =
                SpotifyRecentlyPlayedRepositoryImpl(dataSourceReturning(Result.success(page("t1", "t2"))))

            val result = repository.getRecentlyPlayed()

            assertTrue(result.isSuccess)
            assertEquals(listOf("t1", "t2"), result.getOrThrow().items.map { it.track.id })
        }

    @Test
    fun `the repository publishes what it returns`() =
        runTest {
            val repository =
                SpotifyRecentlyPlayedRepositoryImpl(dataSourceReturning(Result.success(page("t1"))))

            repository.recentlyPlayed.test {
                assertNull(awaitItem())
                val result = repository.getRecentlyPlayed().getOrThrow()
                assertEquals(result, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `an empty page is published as an empty history, not as null`() =
        runTest {
            val repository =
                SpotifyRecentlyPlayedRepositoryImpl(dataSourceReturning(Result.success(RecentlyPlayedResponseDto())))

            repository.getRecentlyPlayed()

            assertTrue(repository.recentlyPlayed.value?.items?.isEmpty() == true)
        }

    @Test
    fun `the repository surfaces a failure and publishes nothing`() =
        runTest {
            val cause = IOException("offline")
            val repository = SpotifyRecentlyPlayedRepositoryImpl(dataSourceReturning(Result.failure(cause)))

            val result = repository.getRecentlyPlayed()

            assertSame(cause, result.exceptionOrNull())
            assertNull(repository.recentlyPlayed.value)
        }

    @Test
    fun `a failure after a success keeps the last published history in the repository`() =
        runTest {
            val api = mockk<IRemoteRecentlyPlayedDataSource>()
            coEvery { api.fetchRecentlyPlayed() } returnsMany
                listOf(Result.success(page("t1", "t2")), Result.failure(IOException("offline")))
            val repository = SpotifyRecentlyPlayedRepositoryImpl(api)

            repository.getRecentlyPlayed()
            val second = repository.getRecentlyPlayed()

            assertTrue(second.isFailure)
            assertEquals(listOf("t1", "t2"), repository.recentlyPlayed.value?.items?.map { it.track.id })
        }

    @Test
    fun `each call hits the data source once`() =
        runTest {
            val api = dataSourceReturning(Result.success(page("t1")))
            val repository = SpotifyRecentlyPlayedRepositoryImpl(api)

            repository.getRecentlyPlayed()
            repository.getRecentlyPlayed()

            coVerify(exactly = 2) { api.fetchRecentlyPlayed() }
        }

    @Test
    fun `the context URI of a play survives the mapping`() =
        runTest {
            val dto =
                RecentlyPlayedResponseDto(
                    items =
                        listOf(
                            PlayHistoryDto(
                                track = trackDto(id = "t1"),
                                playedAt = "2026-09-18T09:03:41Z",
                                context = PlayContextDto(type = "playlist", uri = "spotify:playlist:p1"),
                            ),
                        ),
                )
            val repository = SpotifyRecentlyPlayedRepositoryImpl(dataSourceReturning(Result.success(dto)))

            val play = repository.getRecentlyPlayed().getOrThrow().items.single()

            assertEquals("p1", play.contextUri?.id)
        }

    private fun dataSourceReturning(result: Result<RecentlyPlayedResponseDto>): IRemoteRecentlyPlayedDataSource {
        val api = mockk<IRemoteRecentlyPlayedDataSource>()
        coEvery { api.fetchRecentlyPlayed() } returns result
        return api
    }

    private fun page(vararg trackIds: String) =
        RecentlyPlayedResponseDto(
            items =
                trackIds.map { id ->
                    PlayHistoryDto(track = trackDto(id = id), playedAt = "2026-09-18T09:03:41Z", context = null)
                },
        )

    private fun recentlyPlayed(trackIds: List<String>): RecentlyPlayed =
        RecentlyPlayed(
            items =
                trackIds.map { id ->
                    RecentPlay(
                        track = track(id),
                        playedAt = "2022-01-01T00:00:00Z",
                        contextUri = null,
                    )
                },
        )

    private fun track(id: String) =
        Track(
            album = Album.empty(),
            artists =
                listOf(
                    Artist(
                        externalUrls = "",
                        href = "",
                        id = "ar",
                        name = "Artist of $id",
                        type = "artist",
                        uri = "",
                    ),
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

    private class FakeRecentlyPlayedRepository(
        var nextResults: Result<RecentlyPlayed> = Result.success(RecentlyPlayed.empty()),
    ) : RecentlyPlayedRepository {
        private val _recentlyPlayed = MutableStateFlow<RecentlyPlayed?>(null)
        override val recentlyPlayed = _recentlyPlayed.asStateFlow()

        override suspend fun getRecentlyPlayed(): Result<RecentlyPlayed> = nextResults
    }
}
