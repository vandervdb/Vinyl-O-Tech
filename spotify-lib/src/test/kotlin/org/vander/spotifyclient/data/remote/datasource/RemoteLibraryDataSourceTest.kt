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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.vander.core.domain.auth.ITokenProvider
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
    fun `saveTrack reports success on an HTTP error — known defect`() =
        runTest {
            // Records a real defect, NOT the intended behaviour. Ktor's `expectSuccess` is
            // false by default, so a 403 ("Premium required") or a 401 never throws, and the
            // try/catch has nothing to catch: the failure is reported as a success.
            //
            // Visible consequence: PlayerViewModelImpl.saveTrack flips the local heart on
            // `onSuccess`, so the UI claims the track was saved when Spotify refused.
            val engine =
                MockEngine {
                    respond("""{"error":{"status":403,"message":"Premium required"}}""", HttpStatusCode.Forbidden)
                }

            val result = dataSource(engine).saveTrack(TRACK_ID)

            assertTrue(result.isSuccess)
        }

    @Test
    fun `removeTrack reports success on an HTTP error too — known defect`() =
        runTest {
            val engine = MockEngine { respond("", HttpStatusCode.Unauthorized) }

            assertTrue(dataSource(engine).removeTrack(TRACK_ID).isSuccess)
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
        tokenProvider = FakeTokenProvider(TOKEN),
        logger = logger,
    )

    private class FakeTokenProvider(
        private val token: String?,
    ) : ITokenProvider {
        override val tokenFlow: Flow<String?> = flowOf(token)

        override suspend fun getAccessToken(): String? = token
    }

    private companion object {
        const val BASE_URL = "https://api.spotify.com/v1/"

        const val TRACK_ID = "4cOdK2wGLETKBW3PvgPWqT"

        const val TOKEN = "BQD-fake-access-token"

        const val TAG = "RemoteLibraryDataSource"
    }
}
