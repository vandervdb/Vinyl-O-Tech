package org.vander.spotifyclient.data.repository

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.vander.spotifyclient.fixtures.queueDto
import org.vander.spotifyclient.fixtures.trackDto
import java.io.IOException

class SpotifyQueueRepositoryTest {
    @Test
    fun `a fetched queue is mapped to the domain model`() =
        runTest {
            val repository =
                SpotifyQueueRepository {
                    Result.success(
                        queueDto(currentlyPlaying = trackDto(id = "now"), queue = listOf(trackDto(id = "next"))),
                    )
                }

            val result = repository.refresh()

            assertTrue(result.isSuccess)
            val published = repository.currentQueue.value
            assertEquals("now", published?.currentlyPlaying?.id)
            assertEquals(listOf("next"), published?.queue?.tracks?.map { it.id })
        }

    @Test
    fun `a hole in the queue survives the mapping as an empty track`() =
        runTest {
            val repository =
                SpotifyQueueRepository {
                    Result.success(queueDto(queue = listOf(trackDto(id = "a"), null, trackDto(id = "b"))))
                }

            repository.refresh()

            val tracks =
                repository.currentQueue.value
                    ?.queue
                    ?.tracks
                    .orEmpty()
            assertEquals(3, tracks.size)
            assertEquals("", tracks[1].id)
        }

    @Test
    fun `the cache starts empty and is filled by a successful fetch`() =
        runTest {
            val repository = SpotifyQueueRepository { Result.success(queueDto()) }

            repository.currentQueue.test {
                assertNull(awaitItem())

                repository.refresh()

                assertNotNull(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `a data source failure comes back as a failed Result`() =
        runTest {
            val boom = IOException("offline")
            val repository = SpotifyQueueRepository { Result.failure(boom) }

            val result = repository.refresh()

            assertTrue(result.isFailure)
            assertEquals(boom, result.exceptionOrNull())
        }

    @Test
    fun `a failed refresh leaves the cached queue untouched`() =
        runTest {
            var failing = false
            val repository =
                SpotifyQueueRepository {
                    if (failing) Result.failure(IOException("offline")) else Result.success(queueDto())
                }

            repository.refresh()
            failing = true
            repository.refresh()

            assertNotNull(repository.currentQueue.value)
        }
}
