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

class SpotifyQueueRepositoryImplTest {
    @Test
    fun `a fetched queue is mapped to the domain model`() =
        runTest {
            val repository =
                SpotifyQueueRepositoryImpl {
                    Result.success(
                        queueDto(currentlyPlaying = trackDto(id = "now"), queue = listOf(trackDto(id = "next"))),
                    )
                }

            val result = repository.getUserQueue()

            assertEquals("now", result.getOrThrow().currentlyPlaying?.id)
            assertEquals(
                listOf("next"),
                result
                    .getOrThrow()
                    .queue.tracks
                    .map { it.id },
            )
        }

    @Test
    fun `a hole in the queue survives the mapping as an empty track`() =
        runTest {
            val repository =
                SpotifyQueueRepositoryImpl {
                    Result.success(queueDto(queue = listOf(trackDto(id = "a"), null, trackDto(id = "b"))))
                }

            val tracks =
                repository
                    .getUserQueue()
                    .getOrThrow()
                    .queue.tracks

            assertEquals(3, tracks.size)
            assertEquals("", tracks[1].id)
        }

    @Test
    fun `the cache starts empty and is filled by a successful fetch`() =
        runTest {
            val repository = SpotifyQueueRepositoryImpl { Result.success(queueDto()) }

            repository.currentQueue.test {
                assertNull(awaitItem())

                repository.getUserQueue()

                assertNotNull(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `a data source failure comes back as a failed Result`() =
        runTest {
            val boom = IOException("offline")
            val repository = SpotifyQueueRepositoryImpl { Result.failure(boom) }

            val result = repository.getUserQueue()

            assertTrue(result.isFailure)
            assertEquals(boom, result.exceptionOrNull())
        }

    @Test
    fun `a failed refresh leaves the cached queue untouched`() =
        runTest {
            var failing = false
            val repository =
                SpotifyQueueRepositoryImpl {
                    if (failing) Result.failure(IOException("offline")) else Result.success(queueDto())
                }

            repository.getUserQueue()
            failing = true
            repository.getUserQueue()

            assertNotNull(repository.currentQueue.value)
        }
}
