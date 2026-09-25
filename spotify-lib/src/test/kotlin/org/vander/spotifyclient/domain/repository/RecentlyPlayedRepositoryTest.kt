package org.vander.spotifyclient.domain.repository

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.vander.core.domain.recent.RecentlyPlayedRepository
import org.vander.core.dto.PlayContextDto
import org.vander.core.dto.PlayHistoryDto
import org.vander.core.dto.RecentlyPlayedResponseDto
import org.vander.spotifyclient.data.repository.SpotifyRecentlyPlayedRepository
import org.vander.spotifyclient.domain.datasource.IRemoteRecentlyPlayedDataSource
import org.vander.spotifyclient.fixtures.trackDto
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * The [RecentlyPlayedRepository] contract, exercised through the Spotify adapter: the history
 * only travels on [RecentlyPlayedRepository.recentlyPlayed], [RecentlyPlayedRepository.refresh]
 * only reports the outcome.
 */
class RecentlyPlayedRepositoryTest {
    @Test
    fun `recentlyPlayed starts empty rather than null`() {
        val repository = repository(dataSourceReturning(Result.success(page("t1"))))

        assertTrue(
            repository.recentlyPlayed.value.items
                .isEmpty(),
        )
    }

    @Test
    fun `a successful refresh publishes the mapped history`() =
        runTest {
            val repository = repository(dataSourceReturning(Result.success(page("t1", "t2"))))

            val result = repository.refresh()

            assertTrue(result.isSuccess)
            assertEquals(
                listOf("t1", "t2"),
                repository.recentlyPlayed.value.items
                    .map { it.track.id },
            )
        }

    @Test
    fun `the flow emits once per successful refresh`() =
        runTest {
            val repository = repository(dataSourceReturning(Result.success(page("t1", "t2", "t3"))))

            repository.recentlyPlayed.test {
                assertTrue(awaitItem().items.isEmpty())
                repository.refresh()
                assertEquals(listOf("t1", "t2", "t3"), awaitItem().items.map { it.track.id })
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `a second successful refresh replaces the first one`() =
        runTest {
            val api = mockk<IRemoteRecentlyPlayedDataSource>()
            coEvery { api.fetchRecentlyPlayed() } returnsMany
                listOf(Result.success(page("t1", "t2")), Result.success(page("t3")))
            val repository = repository(api)

            repository.refresh()
            repository.refresh()

            assertEquals(
                listOf("t3"),
                repository.recentlyPlayed.value.items
                    .map { it.track.id },
            )
        }

    @Test
    fun `an empty page is published as an empty history`() =
        runTest {
            val repository = repository(dataSourceReturning(Result.success(RecentlyPlayedResponseDto())))

            val result = repository.refresh()

            assertTrue(result.isSuccess)
            assertTrue(
                repository.recentlyPlayed.value.items
                    .isEmpty(),
            )
        }

    @Test
    fun `a failure is returned with the original exception`() =
        runTest {
            val cause = IOException("offline")
            val repository = repository(dataSourceReturning(Result.failure(cause)))

            val result = repository.refresh()

            assertSame(cause, result.exceptionOrNull())
        }

    @Test
    fun `a failure emits nothing on the flow`() =
        runTest {
            val repository = repository(dataSourceReturning(Result.failure(IOException("offline"))))

            repository.recentlyPlayed.test {
                assertTrue(awaitItem().items.isEmpty())
                repository.refresh()
                expectNoEvents()
            }
        }

    @Test
    fun `a failure after a success keeps the last published history`() =
        runTest {
            // Unlike the profile, the history is not reset on failure: offline, the screen
            // keeps showing what it last had instead of going blank.
            val api = mockk<IRemoteRecentlyPlayedDataSource>()
            coEvery { api.fetchRecentlyPlayed() } returnsMany
                listOf(Result.success(page("t1", "t2")), Result.failure(IOException("offline")))
            val repository = repository(api)

            repository.refresh()
            val second = repository.refresh()

            assertTrue(second.isFailure)
            assertEquals(
                listOf("t1", "t2"),
                repository.recentlyPlayed.value.items
                    .map { it.track.id },
            )
        }

    @Test
    fun `each refresh hits the data source once`() =
        runTest {
            val api = dataSourceReturning(Result.success(page("t1")))
            val repository = repository(api)

            repository.refresh()
            repository.refresh()

            coVerify(exactly = 2) { api.fetchRecentlyPlayed() }
        }

    @Test
    fun `the context URI of a play survives the mapping`() =
        runTest {
            val repository =
                repository(dataSourceReturning(Result.success(pageOf(play("t1", "spotify:playlist:p1")))))

            repository.refresh()

            assertEquals(
                "p1",
                repository.recentlyPlayed.value.items
                    .single()
                    .contextUri
                    ?.id,
            )
        }

    @Test
    fun `lastResumable of the published history skips plays without context`() =
        runTest {
            val repository =
                repository(
                    dataSourceReturning(
                        Result.success(pageOf(play("alone"), play("fromPlaylist", "spotify:playlist:p1"))),
                    ),
                )

            repository.refresh()

            assertEquals(
                "fromPlaylist",
                repository.recentlyPlayed.value.lastResumable
                    ?.track
                    ?.id,
            )
        }

    @Test
    fun `lastResumable is null when no play has a context`() =
        runTest {
            val repository = repository(dataSourceReturning(Result.success(page("a", "b"))))

            repository.refresh()

            assertNull(repository.recentlyPlayed.value.lastResumable)
        }

    private fun repository(api: IRemoteRecentlyPlayedDataSource): RecentlyPlayedRepository =
        SpotifyRecentlyPlayedRepository(api)

    private fun dataSourceReturning(result: Result<RecentlyPlayedResponseDto>): IRemoteRecentlyPlayedDataSource {
        val api = mockk<IRemoteRecentlyPlayedDataSource>()
        coEvery { api.fetchRecentlyPlayed() } returns result
        return api
    }

    private fun page(vararg trackIds: String) = pageOf(*trackIds.map { play(it) }.toTypedArray())

    private fun pageOf(vararg plays: PlayHistoryDto) = RecentlyPlayedResponseDto(items = plays.toList())

    private fun play(
        trackId: String,
        contextUri: String? = null,
    ) = PlayHistoryDto(
        track = trackDto(id = trackId),
        playedAt = "2026-09-18T09:03:41Z",
        context = contextUri?.let { PlayContextDto(type = "playlist", uri = it) },
    )
}
