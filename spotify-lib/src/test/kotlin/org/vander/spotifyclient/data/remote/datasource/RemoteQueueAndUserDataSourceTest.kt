package org.vander.spotifyclient.data.remote.datasource

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
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

/**
 * The two remaining `GET` data sources, which share their shape with
 * [RemotePlaylistDataSource]: one call, one delegation to `parseSpotifyResult`.
 */
class RemoteQueueAndUserDataSourceTest {
    // --- Queue

    @Test
    fun `the queue call goes to me-player-queue`() =
        runTest {
            val engine = jsonEngine(QUEUE_JSON)

            RemoteQueueDataSource(clientOf(engine), token()).fetchUserQueue()

            assertEquals(
                "/v1/me/player/queue",
                engine.requestHistory
                    .single()
                    .url.encodedPath,
            )
        }

    @Test
    fun `a queue payload is parsed, holes included`() =
        runTest {
            val result = RemoteQueueDataSource(clientOf(jsonEngine(QUEUE_JSON)), token()).fetchUserQueue()

            val dto = result.getOrThrow()
            assertEquals("current", dto.currentlyPlaying?.id)
            assertEquals(2, dto.queue.size)
            assertNull(dto.queue[1])
        }

    @Test
    fun `an empty queue payload still parses`() =
        runTest {
            // Both fields default on the DTO, so `{}` is a valid answer — which is what the
            // API returns when nothing is playing.
            val result = RemoteQueueDataSource(clientOf(jsonEngine("{}")), token()).fetchUserQueue()

            assertTrue(result.isSuccess)
            assertNull(result.getOrThrow().currentlyPlaying)
            assertTrue(result.getOrThrow().queue.isEmpty())
        }

    @Test
    fun `a queue error envelope comes back as a failure`() =
        runTest {
            val engine = jsonEngine("""{"error":{"status":401,"message":"expired"}}""")

            assertTrue(RemoteQueueDataSource(clientOf(engine), token()).fetchUserQueue().isFailure)
        }

    // --- User

    @Test
    fun `the user call goes to me`() =
        runTest {
            val engine = jsonEngine(USER_JSON)

            RemoteUserDataSource(clientOf(engine), token()).fetchUser()

            assertEquals(
                "/v1/me",
                engine.requestHistory
                    .single()
                    .url.encodedPath,
            )
        }

    @Test
    fun `a user payload is parsed`() =
        runTest {
            val result = RemoteUserDataSource(clientOf(jsonEngine(USER_JSON)), token()).fetchUser()

            assertEquals("Vander", result.getOrThrow().displayName)
            assertEquals("premium", result.getOrThrow().product)
        }

    @Test
    fun `a user payload missing a required field fails`() =
        runTest {
            // UserDto declares no optional field, so a scope-dependent key the API omits
            // makes the whole call fail rather than yield a partial user — documented on the DTO.
            val engine = jsonEngine("""{"display_name":"Vander"}""")

            assertTrue(RemoteUserDataSource(clientOf(engine), token()).fetchUser().isFailure)
        }

    @Test
    fun `a user error envelope comes back as a failure`() =
        runTest {
            val engine = jsonEngine("""{"error":{"status":403,"message":"forbidden"}}""")

            assertTrue(RemoteUserDataSource(clientOf(engine), token()).fetchUser().isFailure)
        }

    private fun jsonEngine(body: String) =
        MockEngine {
            respond(
                content = body,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

    private fun clientOf(engine: MockEngine) = HttpClient(engine) { defaultRequest { url(BASE_URL) } }

    private fun token() = FakeTokenProvider("BQD-fake-access-token")

    private class FakeTokenProvider(
        private val token: String?,
    ) : ITokenProvider {
        override val tokenFlow: Flow<String?> = flowOf(token)

        override suspend fun getAccessToken(): String? = token
    }

    private companion object {
        const val BASE_URL = "https://api.spotify.com/v1/"

        val TRACK_JSON = { id: String ->
            """
            {
              "album": {
                "album_type": "album", "total_tracks": 11, "available_markets": ["FR"],
                "external_urls": { "spotify": "https://open.spotify.com/album/al" },
                "href": "https://api.spotify.com/v1/albums/al", "id": "al",
                "images": [ { "url": "https://i.scdn.co/c", "height": 640, "width": 640 } ],
                "name": "Nuits blanches", "release_date": "2023-04-14",
                "release_date_precision": "day", "type": "album", "uri": "spotify:album:al",
                "artists": [ { "external_urls": { "spotify": "u" }, "href": "h", "id": "ar", "name": "Elia Faure", "type": "artist", "uri": "spotify:artist:ar" } ]
              },
              "artists": [ { "external_urls": { "spotify": "u" }, "href": "h", "id": "ar", "name": "Elia Faure", "type": "artist", "uri": "spotify:artist:ar" } ],
              "available_markets": ["FR"], "disc_number": 1, "duration_ms": 214000, "explicit": false,
              "external_ids": { "isrc": "FRX122300001" },
              "external_urls": { "spotify": "https://open.spotify.com/track/$id" },
              "href": "https://api.spotify.com/v1/tracks/$id", "id": "$id", "name": "Nuits blanches",
              "popularity": 42, "preview_url": null, "track_number": 3, "type": "track",
              "uri": "spotify:track:$id", "is_local": false
            }
            """.trimIndent()
        }

        val QUEUE_JSON =
            """
            { "currently_playing": ${TRACK_JSON("current")}, "queue": [ ${TRACK_JSON("next")}, null ] }
            """.trimIndent()

        val USER_JSON =
            """
            {
              "country": "FR", "display_name": "Vander", "email": "vander@example.org",
              "explicit_content": { "filter_enabled": false, "filter_locked": false },
              "external_urls": { "spotify": "https://open.spotify.com/user/vander" },
              "followers": { "href": null, "total": 7 },
              "href": "https://api.spotify.com/v1/users/vander", "id": "vander",
              "images": [ { "url": "https://i.scdn.co/avatar", "height": 300, "width": 300 } ],
              "product": "premium", "type": "user", "uri": "spotify:user:vander"
            }
            """.trimIndent()
    }
}
