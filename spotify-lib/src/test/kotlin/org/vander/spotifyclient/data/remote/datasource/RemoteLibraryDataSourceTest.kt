package org.vander.spotifyclient.data.remote.datasource

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.vander.core.logger.test.FakeLogger

class RemoteLibraryDataSourceTest {
    // --- fetchIsTrackSaved

    @Test
    fun `a saved track reads true from the first array element`() =
        runTest {
            // me/tracks/contains answers with an array aligned on the requested ids.
            val engine = jsonEngine("[true]")

            val result = dataSource(engine).fetchIsTrackSaved(TRACK_ID)

            assertTrue(result.getOrThrow())
        }

    @Test
    fun `an unsaved track reads false`() =
        runTest {
            val result = dataSource(jsonEngine("[false]")).fetchIsTrackSaved(TRACK_ID)

            assertFalse(result.getOrThrow())
        }

    @Test
    fun `an empty array reads false rather than failing`() =
        runTest {
            val result = dataSource(jsonEngine("[]")).fetchIsTrackSaved(TRACK_ID)

            assertFalse(result.getOrThrow())
        }

    @Test
    fun `the track id is sent as the ids parameter`() =
        runTest {
            val engine = jsonEngine("[true]")

            dataSource(engine).fetchIsTrackSaved(TRACK_ID)

            val request = engine.requestHistory.single()
            assertEquals("/v1/me/tracks/contains", request.url.encodedPath)
            assertEquals(TRACK_ID, request.url.parameters["ids"])
        }

    @Test
    fun `a body that is not a boolean array fails and is logged`() =
        runTest {
            val logger = FakeLogger()

            val result =
                dataSource(
                    jsonEngine("""{"error":{"status":401,"message":"expired"}}"""),
                    logger,
                ).fetchIsTrackSaved(TRACK_ID)

            assertTrue(result.isFailure)
            assertTrue(logger.contains(FakeLogger.Entry.Level.ERROR, TAG, "Error checking if track is saved"))
        }

    // --- saveTrack / removeTrack

    @Test
    fun `saveTrack sends a PUT on me-tracks with the id`() =
        runTest {
            val engine = jsonEngine("")

            dataSource(engine).saveTrack(TRACK_ID)

            val request = engine.requestHistory.single()
            assertEquals(HttpMethod.Put, request.method)
            assertEquals("/v1/me/tracks", request.url.encodedPath)
            assertEquals(TRACK_ID, request.url.parameters["ids"])
        }

    @Test
    fun `removeTrack sends a DELETE on me-tracks with the id`() =
        runTest {
            val engine = jsonEngine("")

            dataSource(engine).removeTrack(TRACK_ID)

            val request = engine.requestHistory.single()
            assertEquals(HttpMethod.Delete, request.method)
            assertEquals("/v1/me/tracks", request.url.encodedPath)
            assertEquals(TRACK_ID, request.url.parameters["ids"])
        }

    @Test
    fun `saveTrack succeeds on a 200`() =
        runTest {
            assertTrue(dataSource(jsonEngine("")).saveTrack(TRACK_ID).isSuccess)
        }

    @Test
    fun `saveTrack fails on an HTTP error`() =
        runTest {
            // Ktor's `expectSuccess` is false by default, so a refusal never throws and the
            // try/catch has nothing to catch — the status has to be inspected. Without it,
            // PlayerViewModelImpl.saveTrack flips the local heart on `onSuccess` and the UI
            // claims the track was saved when Spotify refused.
            val engine =
                MockEngine {
                    respond("""{"error":{"status":403,"message":"Premium required"}}""", HttpStatusCode.Forbidden)
                }

            val result = dataSource(engine).saveTrack(TRACK_ID)

            assertTrue(result.isFailure)
            assertTrue(
                result
                    .exceptionOrNull()
                    ?.message
                    .orEmpty()
                    .contains("403"),
            )
        }

    @Test
    fun `a failed saveTrack is logged`() =
        runTest {
            val logger = FakeLogger()
            val engine = MockEngine { respond("", HttpStatusCode.Forbidden) }

            dataSource(engine, logger).saveTrack(TRACK_ID)

            assertTrue(logger.contains(FakeLogger.Entry.Level.ERROR, TAG, "Error saving track"))
        }

    @Test
    fun `removeTrack succeeds on a 200`() =
        runTest {
            assertTrue(dataSource(jsonEngine("")).removeTrack(TRACK_ID).isSuccess)
        }

    @Test
    fun `removeTrack fails on an HTTP error`() =
        runTest {
            val engine = MockEngine { respond("", HttpStatusCode.Unauthorized) }

            assertTrue(dataSource(engine).removeTrack(TRACK_ID).isFailure)
        }

    @Test
    fun `a 204 is a success, not an error`() =
        runTest {
            // Spotify answers 204 No Content on a successful library write.
            val engine = MockEngine { respond("", HttpStatusCode.NoContent) }

            assertTrue(dataSource(engine).saveTrack(TRACK_ID).isSuccess)
        }

    private fun jsonEngine(body: String) =
        MockEngine {
            respond(
                content = body,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

    private fun dataSource(
        engine: MockEngine,
        logger: FakeLogger = FakeLogger(),
    ) = RemoteLibraryDataSource(
        httpClient = HttpClient(engine) { defaultRequest { url(BASE_URL) } },
        logger = logger,
    )

    private companion object {
        const val BASE_URL = "https://api.spotify.com/v1/"

        const val TRACK_ID = "4cOdK2wGLETKBW3PvgPWqT"

        const val TAG = "RemoteLibraryDataSource"
    }
}
