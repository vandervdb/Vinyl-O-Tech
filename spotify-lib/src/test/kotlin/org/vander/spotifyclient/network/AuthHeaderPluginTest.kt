package org.vander.spotifyclient.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.vander.core.domain.auth.ITokenProvider

/**
 * This plugin is what actually authenticates every Web API call — the data sources' own
 * `headers { }` block never reaches the request (see `RemotePlaylistDataSourceTest`).
 */
class AuthHeaderPluginTest {
    @Test
    fun `a bearer header is added to the request`() =
        runTest {
            val engine = okEngine()

            clientWith(engine, TOKEN).get("http://localhost/me")

            assertEquals("Bearer $TOKEN", engine.requestHistory.single().headers[HttpHeaders.Authorization])
        }

    @Test
    fun `every request of the client gets the header, not just the first`() =
        runTest {
            val engine = okEngine()
            val client = clientWith(engine, TOKEN)

            client.get("http://localhost/me")
            client.get("http://localhost/me/playlists")

            assertEquals(2, engine.requestHistory.size)
            assertTrue(engine.requestHistory.all { it.headers[HttpHeaders.Authorization] == "Bearer $TOKEN" })
        }

    @Test
    fun `a null token omits the header and lets the call through`() =
        runTest {
            // Documented behaviour: the plugin never short-circuits a call.
            val engine = okEngine()

            clientWith(engine, null).get("http://localhost/me")

            assertNull(engine.requestHistory.single().headers[HttpHeaders.Authorization])
            assertEquals(1, engine.requestHistory.size)
        }

    @Test
    fun `a blank token omits the header too`() =
        runTest {
            val engine = okEngine()

            clientWith(engine, "   ").get("http://localhost/me")

            assertNull(engine.requestHistory.single().headers[HttpHeaders.Authorization])
        }

    @Test
    fun `the token is read per request, so a refresh is picked up`() =
        runTest {
            // The interceptor calls getAccessToken() on every request rather than capturing
            // it at install time — that is what makes a refreshed token take effect without
            // rebuilding the client.
            val engine = okEngine()
            val provider = MutableTokenProvider("first")
            val client = clientWith(engine, provider)

            client.get("http://localhost/a")
            provider.token = "second"
            client.get("http://localhost/b")

            assertEquals("Bearer first", engine.requestHistory[0].headers[HttpHeaders.Authorization])
            assertEquals("Bearer second", engine.requestHistory[1].headers[HttpHeaders.Authorization])
        }

    @Test
    fun `installing without a token provider fails fast`() =
        runTest {
            val exception =
                assertThrows(IllegalArgumentException::class.java) {
                    HttpClient(MockEngine { respond("{}") }) { install(AuthHeaderPlugin) {} }
                }

            assertTrue(exception.message.orEmpty().contains("ITokenProvider"))
        }

    private fun okEngine() = MockEngine { respond("{}") }

    private fun clientWith(
        engine: MockEngine,
        token: String?,
    ) = clientWith(engine, MutableTokenProvider(token))

    private fun clientWith(
        engine: MockEngine,
        provider: ITokenProvider,
    ) = HttpClient(engine) {
        install(AuthHeaderPlugin) { tokenProvider = provider }
    }

    private class MutableTokenProvider(
        var token: String?,
    ) : ITokenProvider {
        override val tokenFlow: Flow<String?> get() = flowOf(token)

        override suspend fun getAccessToken(): String? = token
    }

    private companion object {
        const val TOKEN = "BQD-fake-access-token"
    }
}
