package org.vander.spotifyclient.data.remote.datasource

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.vander.core.domain.auth.ITokenProvider
import org.vander.spotifyclient.network.AuthHeaderPlugin

/**
 * Drives the real Ktor call through `MockEngine`, so the request that would go on the wire
 * is inspectable: URL, path and headers.
 */
class RemotePlaylistDataSourceTest {
    @Test
    fun `a page of playlists is parsed into the DTO`() =
        runTest {
            val engine = jsonEngine(PLAYLIST_PAGE_JSON)

            val result = RemotePlaylistDataSource(clientOf(engine), FakeTokenProvider(TOKEN)).fetchUserPlaylists()

            assertTrue(result.isSuccess)
            assertEquals(listOf("Sillons"), result.getOrThrow().items.map { it.name })
        }

    @Test
    fun `the call goes to me-playlists`() =
        runTest {
            val engine = jsonEngine(PLAYLIST_PAGE_JSON)

            RemotePlaylistDataSource(clientOf(engine), FakeTokenProvider(TOKEN)).fetchUserPlaylists()

            assertEquals(
                "/v1/me/playlists",
                engine.requestHistory
                    .single()
                    .url.encodedPath,
            )
        }

    @Test
    fun `the data source does not actually set the Authorization header — known defect`() =
        runTest {
            // Records a real defect, it is NOT the intended behaviour. The data source writes
            // `headers { append(Authorization, ...) }` while importing `io.ktor.http.headers`,
            // the top-level builder — which returns a `Headers` object that is then discarded.
            // `AuthRemoteDataSource` imports `io.ktor.client.request.headers` instead and does
            // reach the request, which is what makes the difference visible inside the repo.
            //
            // Production is unaffected only because the `auth_api_v1_client` installs
            // AuthHeaderPlugin (see the next test). Point a data source at a client built with
            // `enableAuthPlugin = false` and every call goes out unauthenticated, silently.
            val engine = jsonEngine(PLAYLIST_PAGE_JSON)

            RemotePlaylistDataSource(clientOf(engine), FakeTokenProvider(TOKEN)).fetchUserPlaylists()

            assertNull(engine.requestHistory.single().headers[HttpHeaders.Authorization])
        }

    @Test
    fun `the plugin is what authenticates the call in production`() =
        runTest {
            val engine = jsonEngine(PLAYLIST_PAGE_JSON)
            val client = clientOf(engine) { install(AuthHeaderPlugin) { tokenProvider = FakeTokenProvider(TOKEN) } }

            RemotePlaylistDataSource(client, FakeTokenProvider(TOKEN)).fetchUserPlaylists()

            assertEquals("Bearer $TOKEN", engine.requestHistory.single().headers[HttpHeaders.Authorization])
        }

    @Test
    fun `a blank token means no header at all, and the call still goes out`() =
        runTest {
            // AuthHeaderPlugin skips a null-or-blank token rather than short-circuiting.
            val engine = jsonEngine(PLAYLIST_PAGE_JSON)
            val client = clientOf(engine) { install(AuthHeaderPlugin) { tokenProvider = FakeTokenProvider(null) } }

            val result = RemotePlaylistDataSource(client, FakeTokenProvider(null)).fetchUserPlaylists()

            assertNull(engine.requestHistory.single().headers[HttpHeaders.Authorization])
            assertTrue(result.isSuccess)
        }

    @Test
    fun `a spotify error envelope comes back as a failure`() =
        runTest {
            val engine = jsonEngine("""{"error":{"status":401,"message":"The access token expired"}}""")

            val result = RemotePlaylistDataSource(clientOf(engine), FakeTokenProvider(TOKEN)).fetchUserPlaylists()

            assertTrue(result.isFailure)
            assertTrue(
                result
                    .exceptionOrNull()
                    ?.message
                    .orEmpty()
                    .contains("401"),
            )
        }

    @Test
    fun `a malformed body comes back as a failure`() =
        runTest {
            val engine = jsonEngine("{ not json")

            val result = RemotePlaylistDataSource(clientOf(engine), FakeTokenProvider(TOKEN)).fetchUserPlaylists()

            assertTrue(result.isFailure)
        }

    @Test
    fun `a transport error is caught rather than thrown`() =
        runTest {
            // The try/catch around the call is what keeps an exception from escaping the
            // data-source boundary, as the error-handling rule requires.
            val engine = MockEngine { respondError(HttpStatusCode.InternalServerError) }

            val result = RemotePlaylistDataSource(clientOf(engine), FakeTokenProvider(TOKEN)).fetchUserPlaylists()

            assertTrue(result.isFailure)
        }

    private fun jsonEngine(body: String) =
        MockEngine {
            respond(
                content = body,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

    private fun clientOf(
        engine: MockEngine,
        extra: HttpClientConfig<*>.() -> Unit = {},
    ) = HttpClient(engine) {
        defaultRequest { url(BASE_URL) }
        extra()
    }

    private class FakeTokenProvider(
        private val token: String?,
    ) : ITokenProvider {
        override val tokenFlow: Flow<String?> = flowOf(token)

        override suspend fun getAccessToken(): String? = token
    }

    private companion object {
        const val BASE_URL = "https://api.spotify.com/v1/"

        const val TOKEN = "BQD-fake-access-token"

        val PLAYLIST_PAGE_JSON =
            """
            {
              "href": "https://api.spotify.com/v1/me/playlists",
              "limit": 20,
              "next": null,
              "offset": 0,
              "previous": null,
              "total": 1,
              "items": [
                {
                  "collaborative": false,
                  "description": "",
                  "external_urls": { "spotify": "https://open.spotify.com/playlist/p1" },
                  "href": "https://api.spotify.com/v1/playlists/p1",
                  "id": "p1",
                  "images": [ { "url": "https://i.scdn.co/cover", "height": 640, "width": 640 } ],
                  "name": "Sillons",
                  "owner": {
                    "external_urls": { "spotify": "https://open.spotify.com/user/vander" },
                    "href": "https://api.spotify.com/v1/users/vander",
                    "id": "vander",
                    "type": "user",
                    "uri": "spotify:user:vander",
                    "display_name": "Vander"
                  },
                  "public": false,
                  "snapshot_id": "snap",
                  "tracks": { "href": "https://api.spotify.com/v1/playlists/p1/tracks", "total": 11 },
                  "type": "playlist",
                  "uri": "spotify:playlist:p1"
                }
              ]
            }
            """.trimIndent()
    }
}
