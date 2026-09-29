package org.vander.spotifyclient.data.remote.datasource

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.forms.FormDataContent
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.vander.core.logger.test.FakeLogger
import org.vander.spotifyclient.BuildConfig.CLIENT_ID
import org.vander.spotifyclient.BuildConfig.CLIENT_SECRET
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class RemoteAuthDataSourceTest {
    // --- fetchAccessToken

    @Test
    fun `the code exchange authenticates the app with a Basic header`() =
        runTest {
            val engine = jsonEngine(TOKEN_WITH_REFRESH)

            dataSource(engine).fetchAccessToken(CODE)

            val request = engine.requestHistory.single()
            assertBasicCredentials(request.headers[HttpHeaders.Authorization])
            val form = (request.body as FormDataContent).formData
            assertEquals("authorization_code", form["grant_type"])
            assertEquals(CODE, form["code"])
        }

    @Test
    fun `a replayed code fails with the accounts service error, logged`() =
        runTest {
            val logger = FakeLogger()

            val result =
                dataSource(
                    jsonEngine(INVALID_GRANT, HttpStatusCode.BadRequest),
                    logger,
                ).fetchAccessToken(CODE)

            val message = result.exceptionOrNull()?.message.orEmpty()
            assertTrue(message, message.contains("invalid_grant: Invalid authorization code"))
            assertTrue(logger.contains(FakeLogger.Entry.Level.ERROR, TAG, "Error exchanging the authorization code"))
        }

    // --- refreshAccessToken

    @Test
    fun `the refresh authenticates the app with a Basic header, not a client-id field`() =
        runTest {
            // The app uses the client-secret flow: Spotify requires the Basic header on refresh
            // too, and client_id in the body is only for PKCE.
            val engine = jsonEngine(TOKEN_WITHOUT_REFRESH)

            dataSource(engine).refreshAccessToken(REFRESH_TOKEN)

            val request = engine.requestHistory.single()
            assertBasicCredentials(request.headers[HttpHeaders.Authorization])
            val form = (request.body as FormDataContent).formData
            assertEquals("refresh_token", form["grant_type"])
            assertEquals(REFRESH_TOKEN, form["refresh_token"])
            assertNull(form["client-id"])
            assertNull(form["client_id"])
        }

    @Test
    fun `a revoked refresh token fails with the accounts service error, logged`() =
        runTest {
            val logger = FakeLogger()

            val result =
                dataSource(
                    jsonEngine(REVOKED, HttpStatusCode.BadRequest),
                    logger,
                ).refreshAccessToken(REFRESH_TOKEN)

            val message = result.exceptionOrNull()?.message.orEmpty()
            assertTrue(message, message.contains("invalid_grant: Refresh token revoked"))
            assertTrue(logger.contains(FakeLogger.Entry.Level.ERROR, TAG, "Error refreshing the access token"))
        }

    @Test
    fun `a refresh response without a refresh token decodes to a null one`() =
        runTest {
            // Spotify usually keeps the refresh token unchanged and omits it from the response.
            val result = dataSource(jsonEngine(TOKEN_WITHOUT_REFRESH)).refreshAccessToken(REFRESH_TOKEN)

            val token = result.getOrThrow()
            assertEquals("new-access", token.accessToken)
            assertNull(token.refreshToken)
        }

    // assertTrue rather than assertEquals: a failure message must not print the encoded client
    // secret into a test report or a CI log.
    @OptIn(ExperimentalEncodingApi::class)
    private fun assertBasicCredentials(header: String?) {
        val expected = "Basic " + Base64.encode("$CLIENT_ID:$CLIENT_SECRET".toByteArray())
        assertTrue("Authorization is not the app's Basic credentials", header == expected)
    }

    private fun jsonEngine(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ) = MockEngine {
        respond(
            content = body,
            status = status,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
        )
    }

    private fun dataSource(
        engine: MockEngine,
        logger: FakeLogger = FakeLogger(),
    ) = SpotifyRemoteAuthDataSource(
        httpClient = HttpClient(engine) { defaultRequest { url(BASE_URL) } },
        logger = logger,
    )

    private companion object {
        const val BASE_URL = "https://accounts.spotify.com/api/"

        const val CODE = "auth-code"

        const val REFRESH_TOKEN = "refresh-token"

        const val TAG = "SpotifyRemoteAuthDataSource"

        const val TOKEN_WITH_REFRESH =
            """{"access_token":"access","token_type":"Bearer","expires_in":3600,"refresh_token":"refresh"}"""

        const val TOKEN_WITHOUT_REFRESH =
            """{"access_token":"new-access","token_type":"Bearer","expires_in":3600}"""

        const val INVALID_GRANT =
            """{"error":"invalid_grant","error_description":"Invalid authorization code"}"""

        const val REVOKED =
            """{"error":"invalid_grant","error_description":"Refresh token revoked"}"""
    }
}
