package org.vander.spotifyclient.utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.vander.core.logger.test.FakeLogger
import kotlin.test.Test

/**
 * Covers the four branches of [parseSpotifyResult]: the Spotify error envelope, the happy
 * path, a body that will not parse, and the 204 short-circuit of [parseSpotifyResultOrNull].
 */
class HttpResponseParserTest {
    @Serializable
    private data class Payload(
        val id: String,
        val name: String,
    )

    // --- Happy path

    @Test
    fun `a well formed body is deserialized`() =
        runTest {
            val logger = FakeLogger()

            val result = respondWith("""{"id":"42","name":"Nuits blanches"}""").parseSpotifyResult<Payload>(TAG, logger)

            assertTrue(result.isSuccess)
            assertEquals(Payload("42", "Nuits blanches"), result.getOrThrow())
        }

    @Test
    fun `an unknown key does not break the call`() =
        runTest {
            // ignoreUnknownKeys is on so a field added by Spotify cannot break a release.
            val body = """{"id":"42","name":"Nuits blanches","added_by_spotify_later":true}"""

            val result = respondWith(body).parseSpotifyResult<Payload>(TAG, FakeLogger())

            assertEquals("42", result.getOrThrow().id)
        }

    @Test
    fun `the successful branch logs the body at debug level`() =
        runTest {
            // Deliberate behaviour, documented as a warning on the function: this leaks
            // personal data on the `me` endpoints.
            val logger = FakeLogger()

            respondWith("""{"id":"42","name":"x"}""").parseSpotifyResult<Payload>(TAG, logger)

            assertTrue(logger.contains(FakeLogger.Entry.Level.DEBUG, TAG, "Spotify response"))
        }

    // --- Spotify error envelope

    @Test
    fun `an error envelope fails even on a 200`() =
        runTest {
            // The whole reason the body is parsed twice: Spotify answers 200 with an error
            // object in some cases, so the status code alone cannot be trusted.
            val body = """{"error":{"status":401,"message":"The access token expired"}}"""

            val result = respondWith(body, HttpStatusCode.OK).parseSpotifyResult<Payload>(TAG, FakeLogger())

            assertTrue(result.isFailure)
        }

    @Test
    fun `the failure message carries the status and the message from the envelope`() =
        runTest {
            val body = """{"error":{"status":403,"message":"Player command failed: Premium required"}}"""

            val result = respondWith(body).parseSpotifyResult<Payload>(TAG, FakeLogger())

            val message = result.exceptionOrNull()?.message.orEmpty()
            assertTrue(message, message.contains("403"))
            assertTrue(message, message.contains("Premium required"))
        }

    @Test
    fun `an error envelope is logged at error level`() =
        runTest {
            val logger = FakeLogger()
            val body = """{"error":{"status":404,"message":"Not found"}}"""

            respondWith(body).parseSpotifyResult<Payload>(TAG, logger)

            assertTrue(logger.contains(FakeLogger.Entry.Level.ERROR, TAG, "404"))
        }

    // --- Bodies that will not parse

    @Test
    fun `malformed json fails instead of throwing`() =
        runTest {
            val result = respondWith("{ this is not json").parseSpotifyResult<Payload>(TAG, FakeLogger())

            assertTrue(result.isFailure)
        }

    @Test
    fun `an empty body fails`() =
        runTest {
            val result = respondWith("").parseSpotifyResult<Payload>(TAG, FakeLogger())

            assertTrue(result.isFailure)
        }

    @Test
    fun `a json array fails, because the parser expects an object`() =
        runTest {
            // `.jsonObject` throws on anything that is not an object; the catch turns it
            // into a failed Result like any other parsing problem.
            val result = respondWith("""[{"id":"42","name":"x"}]""").parseSpotifyResult<Payload>(TAG, FakeLogger())

            assertTrue(result.isFailure)
        }

    @Test
    fun `a body missing a required field fails`() =
        runTest {
            val result = respondWith("""{"id":"42"}""").parseSpotifyResult<Payload>(TAG, FakeLogger())

            assertTrue(result.isFailure)
        }

    @Test
    fun `a parsing failure is logged at error level`() =
        runTest {
            val logger = FakeLogger()

            respondWith("{ nope").parseSpotifyResult<Payload>(TAG, logger)

            assertTrue(logger.contains(FakeLogger.Entry.Level.ERROR, TAG, "JSON parsing error"))
        }

    @Test
    fun `the tag given by the caller is the one logged`() =
        runTest {
            val logger = FakeLogger()

            respondWith("""{"id":"1","name":"x"}""").parseSpotifyResult<Payload>("SpotifyRemoteUserDataSource", logger)

            assertEquals("SpotifyRemoteUserDataSource", logger.last()?.tag)
        }

    // --- 204 short-circuit

    @Test
    fun `parseSpotifyResultOrNull returns null on 204`() =
        runTest {
            val result = respondWith("", HttpStatusCode.NoContent).parseSpotifyResultOrNull<Payload>(TAG, FakeLogger())

            assertTrue(result.isSuccess)
            assertNull(result.getOrThrow())
        }

    @Test
    fun `parseSpotifyResultOrNull does not touch the body on 204`() =
        runTest {
            // The early return happens before bodyAsText(), so even a body that could not
            // parse is irrelevant — and nothing is logged.
            val logger = FakeLogger()

            val result =
                respondWith(
                    "{ not json",
                    HttpStatusCode.NoContent,
                ).parseSpotifyResultOrNull<Payload>(TAG, logger)

            assertTrue(result.isSuccess)
            assertEquals(0, logger.count())
        }

    @Test
    fun `parseSpotifyResultOrNull delegates when there is content`() =
        runTest {
            val result =
                respondWith("""{"id":"7","name":"Braise"}""").parseSpotifyResultOrNull<Payload>(TAG, FakeLogger())

            assertEquals(Payload("7", "Braise"), result.getOrThrow())
        }

    @Test
    fun `parseSpotifyResultOrNull propagates a failure`() =
        runTest {
            val body = """{"error":{"status":500,"message":"Server error"}}"""

            val result = respondWith(body).parseSpotifyResultOrNull<Payload>(TAG, FakeLogger())

            assertTrue(result.isFailure)
        }

    private suspend fun respondWith(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ): HttpResponse {
        val client = HttpClient(MockEngine { respond(body, status) })
        return client.get("http://localhost/me")
    }

    private companion object {
        const val TAG = "SpotifyApiTest"
    }
}
