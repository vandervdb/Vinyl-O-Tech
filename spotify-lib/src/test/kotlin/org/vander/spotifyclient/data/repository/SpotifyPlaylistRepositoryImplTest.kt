package org.vander.spotifyclient.data.repository

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.vander.spotifyclient.fixtures.playlistDto
import org.vander.spotifyclient.fixtures.playlistPageDto
import java.io.IOException

/**
 * `IRemotePlaylistDataSource` is a `fun interface`, so each test passes a lambda rather
 * than a mock — the data source has one method and nothing to verify beyond what it returns.
 */
class SpotifyPlaylistRepositoryImplTest {
    @Test
    fun `a fetched page is mapped to the domain collection`() =
        runTest {
            val repository =
                SpotifyPlaylistRepositoryImpl {
                    Result.success(playlistPageDto(listOf(playlistDto(id = "a", name = "Sillons"))))
                }

            val result = repository.getUserPlaylists()

            assertTrue(result.isSuccess)
            assertEquals(listOf("Sillons"), result.getOrThrow().items.map { it.name })
        }

    @Test
    fun `the cache starts empty and is filled by a successful fetch`() =
        runTest {
            val repository = SpotifyPlaylistRepositoryImpl { Result.success(playlistPageDto()) }

            repository.playlists.test {
                assertNull(awaitItem())

                repository.getUserPlaylists()

                assertEquals(1, awaitItem()?.items?.size)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `a data source failure comes back as a failed Result`() =
        runTest {
            val boom = IOException("offline")
            val repository = SpotifyPlaylistRepositoryImpl { Result.failure(boom) }

            val result = repository.getUserPlaylists()

            assertTrue(result.isFailure)
            assertEquals(boom, result.exceptionOrNull())
        }

    @Test
    fun `a failed refresh leaves the cached value untouched`() =
        runTest {
            // Documented behaviour of this repository: the cache survives a failed refresh,
            // so the grid keeps showing what it had rather than blanking out.
            var failing = false
            val repository =
                SpotifyPlaylistRepositoryImpl {
                    if (failing) Result.failure(IOException("offline")) else Result.success(playlistPageDto())
                }

            repository.getUserPlaylists()
            val cached = repository.playlists.value
            failing = true
            repository.getUserPlaylists()

            assertNotNull(repository.playlists.value)
            assertEquals(
                cached?.items?.size,
                repository.playlists.value
                    ?.items
                    ?.size,
            )
        }
}
