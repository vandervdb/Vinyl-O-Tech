package org.vander.spotifyclient.data.repository

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.vander.core.dto.UserDto
import org.vander.core.logger.test.FakeLogger
import org.vander.spotifyclient.domain.datasource.IRemoteUserDataSource
import org.vander.spotifyclient.fixtures.userDto
import java.io.IOException

class SpotifyUserRepositoryTest {
    @Test
    fun `a fetched profile is published on currentUser`() =
        runTest {
            val repository = repositoryReturning(Result.success(userDto(displayName = "Vander")))

            repository.fetchCurrentUser()

            assertEquals("Vander", repository.currentUser.first()?.name)
        }

    @Test
    fun `currentUser starts null and receives the profile`() =
        runTest {
            val repository = repositoryReturning(Result.success(userDto(displayName = "Vander")))

            repository.currentUser.test {
                assertNull(awaitItem())

                repository.fetchCurrentUser()

                assertEquals("Vander", awaitItem()?.name)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `a failure resets currentUser to null and logs`() =
        runTest {
            val logger = FakeLogger()
            val repository = repositoryReturning(Result.failure(IOException("offline")), logger)

            repository.fetchCurrentUser()

            assertNull(repository.currentUser.first())
            assertTrue(logger.contains(FakeLogger.Entry.Level.ERROR, TAG, "Error fetching user"))
        }

    @Test
    fun `a previously published profile is cleared by a later failure`() =
        runTest {
            // Unlike the playlist and queue repositories, this one resets its state on
            // failure rather than keeping the last value it had.
            val api = mockk<IRemoteUserDataSource>()
            coEvery { api.fetchUser() } returnsMany
                listOf(Result.success(userDto()), Result.failure(IOException("offline")))
            val repository = SpotifyUserRepository(api, FakeLogger())

            repository.fetchCurrentUser()
            repository.fetchCurrentUser()

            assertNull(repository.currentUser.first())
        }

    @Test
    fun `a cancellation is rethrown rather than swallowed`() =
        runTest {
            // Catching CancellationException would break structured concurrency: it is how
            // cancellation propagates, not a failure to report.
            val api = mockk<IRemoteUserDataSource>()
            coEvery { api.fetchUser() } throws CancellationException("cancelled")

            val thrown = runCatching { SpotifyUserRepository(api, FakeLogger()).fetchCurrentUser() }.exceptionOrNull()

            assertTrue("expected a CancellationException, got $thrown", thrown is CancellationException)
        }

    @Test
    fun `a cancellation is not logged as an error`() =
        runTest {
            val logger = FakeLogger()
            val api = mockk<IRemoteUserDataSource>()
            coEvery { api.fetchUser() } throws CancellationException("cancelled")

            runCatching { SpotifyUserRepository(api, logger).fetchCurrentUser() }

            assertEquals(0, logger.count(FakeLogger.Entry.Level.ERROR))
        }

    private fun repositoryReturning(
        result: Result<UserDto>,
        logger: FakeLogger = FakeLogger(),
    ): SpotifyUserRepository {
        val api = mockk<IRemoteUserDataSource>()
        coEvery { api.fetchUser() } returns result
        return SpotifyUserRepository(api, logger)
    }

    private companion object {
        const val TAG = "SpotifyUserRepository"
    }
}
