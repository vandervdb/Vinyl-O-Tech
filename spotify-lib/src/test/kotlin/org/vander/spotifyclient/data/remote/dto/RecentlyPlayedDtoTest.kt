package org.vander.spotifyclient.data.remote.dto

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.vander.core.dto.RecentlyPlayedResponseDto

/**
 * Parses the `GET /me/player/recently-played` payload from Spotify's own reference, exactly as
 * documented, to pin the DTO against it.
 *
 * Lives in `spotify-lib` rather than in `core:dto`, which has no test source set — and this is
 * where the endpoint will be consumed anyway. `ignoreUnknownKeys` mirrors the real parser in
 * `HttpResponseParser`.
 */
class RecentlyPlayedDtoTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `the documented payload parses`() {
        val dto = json.decodeFromString<RecentlyPlayedResponseDto>(REFERENCE_PAYLOAD)

        assertEquals(1, dto.items.size)
        assertEquals("string", dto.cursors?.after)
        assertEquals("string", dto.items.single().playedAt)
    }

    @Test
    fun `a play carries its track and the context it was played from`() {
        val play = json.decodeFromString<RecentlyPlayedResponseDto>(REFERENCE_PAYLOAD).items.single()

        assertEquals("2up3OPMp9Tb4dAKM2erWXQ", play.track.album.id)
        assertEquals(
            "market",
            play.track.album.restrictions
                ?.reason,
        )
        assertEquals("string", play.context?.uri)
    }

    @Test
    fun `a page with only its items parses, since every other field defaults`() {
        // What the endpoint sends varies with where the page sits; a missing key must not fail
        // the whole call, the way `UserDto` still does.
        val dto = json.decodeFromString<RecentlyPlayedResponseDto>("""{"items":[]}""")

        assertEquals(0, dto.limit)
        assertNull(dto.cursors)
        assertNull(dto.next)
    }

    @Test
    fun `a track played on its own has no context`() {
        val body =
            """{"items":[{"track":""" + trackOf(REFERENCE_PAYLOAD) + ""","played_at":"2026-09-18T07:03:41.769Z"}]}"""

        val play = json.decodeFromString<RecentlyPlayedResponseDto>(body).items.single()

        assertNull(play.context)
    }

    /** Lifts the `track` object out of the reference payload, to reuse it in a shorter case. */
    private fun trackOf(payload: String): String {
        val start = payload.indexOf("\"track\":") + "\"track\":".length
        var depth = 0
        for (index in start until payload.length) {
            when (payload[index]) {
                '{' -> depth++
                '}' -> if (--depth == 0) return payload.substring(start, index + 1)
                else -> Unit
            }
        }
        error("no track object in the payload")
    }

    private companion object {
        val REFERENCE_PAYLOAD =
            """
            {
              "href": "string",
              "limit": 0,
              "next": "string",
              "cursors": { "after": "string", "before": "string" },
              "total": 0,
              "items": [
                {
                  "track": {
                    "album": {
                      "album_type": "compilation",
                      "total_tracks": 9,
                      "available_markets": ["CA", "BR", "IT"],
                      "external_urls": { "spotify": "string" },
                      "href": "string",
                      "id": "2up3OPMp9Tb4dAKM2erWXQ",
                      "images": [
                        { "url": "https://i.scdn.co/image/ab67616d00001e02ff9ca10b55ce82ae553c8228", "height": 300, "width": 300 }
                      ],
                      "name": "string",
                      "release_date": "1981-12",
                      "release_date_precision": "year",
                      "restrictions": { "reason": "market" },
                      "type": "album",
                      "uri": "spotify:album:2up3OPMp9Tb4dAKM2erWXQ",
                      "artists": [
                        { "external_urls": { "spotify": "string" }, "href": "string", "id": "string", "name": "string", "type": "artist", "uri": "string" }
                      ]
                    },
                    "artists": [
                      { "external_urls": { "spotify": "string" }, "href": "string", "id": "string", "name": "string", "type": "artist", "uri": "string" }
                    ],
                    "available_markets": ["string"],
                    "disc_number": 0,
                    "duration_ms": 0,
                    "explicit": false,
                    "external_ids": { "isrc": "string", "ean": "string", "upc": "string" },
                    "external_urls": { "spotify": "string" },
                    "href": "string",
                    "id": "string",
                    "is_playable": false,
                    "linked_from": {},
                    "restrictions": { "reason": "string" },
                    "name": "string",
                    "popularity": 0,
                    "preview_url": "string",
                    "track_number": 0,
                    "type": "track",
                    "uri": "string",
                    "is_local": false
                  },
                  "played_at": "string",
                  "context": {
                    "type": "string",
                    "href": "string",
                    "external_urls": { "spotify": "string" },
                    "uri": "string"
                  }
                }
              ]
            }
            """.trimIndent()
    }
}
