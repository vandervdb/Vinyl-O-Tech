package org.vander.spotifyclient.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.vander.spotifyclient.domain.datasource.IRemoteLibraryDataSource
import java.io.IOException

/**
 * The repository forwards to the data source without transforming anything, so these tests
 * pin exactly that: the same [Result] comes out, and the track id reaches the data source
 * untouched. They are what would catch a "while I'm here" change turning a failure into a
 * default value.
 */
class SpotifyLibraryRepositoryTest {
    private val api = mockk<IRemoteLibraryDataSource>()

    private val repository = SpotifyLibraryRepository(api)

    @Test
    fun `isTrackSaved forwards the id and returns the answer`() =
        runTest {
            coEvery { api.fetchIsTrackSaved(TRACK_ID) } returns Result.success(true)

            assertEquals(true, repository.isTrackSaved(TRACK_ID).getOrThrow())
            coVerify(exactly = 1) { api.fetchIsTrackSaved(TRACK_ID) }
        }

    @Test
    fun `isTrackSaved propagates a failure instead of defaulting to false`() =
        runTest {
            val boom = IOException("offline")
            coEvery { api.fetchIsTrackSaved(TRACK_ID) } returns Result.failure(boom)

            val result = repository.isTrackSaved(TRACK_ID)

            assertTrue(result.isFailure)
            assertEquals(boom, result.exceptionOrNull())
        }

    @Test
    fun `saveTrack forwards the id`() =
        runTest {
            coEvery { api.saveTrack(TRACK_ID) } returns Result.success(Unit)

            assertTrue(repository.saveTrack(TRACK_ID).isSuccess)
            coVerify(exactly = 1) { api.saveTrack(TRACK_ID) }
        }

    @Test
    fun `saveTrack propagates a failure`() =
        runTest {
            coEvery { api.saveTrack(TRACK_ID) } returns Result.failure(IOException("offline"))

            assertTrue(repository.saveTrack(TRACK_ID).isFailure)
        }

    @Test
    fun `removeTrack forwards the id`() =
        runTest {
            coEvery { api.removeTrack(TRACK_ID) } returns Result.success(Unit)

            assertTrue(repository.removeTrack(TRACK_ID).isSuccess)
            coVerify(exactly = 1) { api.removeTrack(TRACK_ID) }
        }

    @Test
    fun `removeTrack propagates a failure`() =
        runTest {
            coEvery { api.removeTrack(TRACK_ID) } returns Result.failure(IOException("offline"))

            assertTrue(repository.removeTrack(TRACK_ID).isFailure)
        }

    private companion object {
        const val TRACK_ID = "4cOdK2wGLETKBW3PvgPWqT"
    }
}
