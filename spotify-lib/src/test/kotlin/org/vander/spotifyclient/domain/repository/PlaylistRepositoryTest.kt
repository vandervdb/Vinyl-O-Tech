package org.vander.spotifyclient.domain.repository
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.vander.core.domain.playlist.PlaylistRepository
import org.vander.spotifyclient.data.remote.datasource.RemotePlaylistDataSource
import org.vander.spotifyclient.data.repository.SpotifyPlaylistRepository
import org.vander.spotifyclient.fixtures.playlistDto
import org.vander.spotifyclient.fixtures.playlistPageDto
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * The [PlaylistRepository] contract, exercised through the Spotify adapter: the data only
 * travels on [PlaylistRepository.playlists], [PlaylistRepository.refresh] only reports the outcome.
 */
class PlaylistRepositoryTest {
    @Test
    fun `playlists starts empty rather than null`() =
        runTest {
            val repository = repository { Result.success(playlistPageDto()) }

            assertTrue(
                repository.playlists.value.items
                    .isEmpty(),
            )
        }

    @Test
    fun `a successful refresh publishes the collection`() =
        runTest {
            val repository =
                repository {
                    Result.success(playlistPageDto(listOf(playlistDto("a", "Sillons"), playlistDto("b", "Braise"))))
                }

            val result = repository.refresh()

            assertTrue(result.isSuccess)
            assertEquals(
                listOf("Sillons", "Braise"),
                repository.playlists.value.items
                    .map { it.name },
            )
        }

    @Test
    fun `the flow emits once per successful refresh`() =
        runTest {
            val repository = repository { Result.success(playlistPageDto(listOf(playlistDto("a", "Sillons")))) }

            repository.playlists.test {
                assertTrue(awaitItem().items.isEmpty())

                repository.refresh()

                assertEquals("Sillons", awaitItem().items.single().name)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `a failure is returned with the original exception`() =
        runTest {
            val boom = IOException("offline")
            val repository = repository { Result.failure(boom) }

            val result = repository.refresh()

            assertSame(boom, result.exceptionOrNull())
        }

    @Test
    fun `a failure after a success keeps what was published`() =
        runTest {
            var failing = false
            val repository =
                repository {
                    if (failing) {
                        Result.failure(IOException("offline"))
                    } else {
                        Result.success(playlistPageDto(listOf(playlistDto("a", "Sillons"))))
                    }
                }

            repository.refresh()
            failing = true
            repository.refresh()

            assertEquals(
                "Sillons",
                repository.playlists.value.items
                    .single()
                    .name,
            )
        }

    private fun repository(dataSource: RemotePlaylistDataSource): PlaylistRepository =
        SpotifyPlaylistRepository(dataSource)
}
