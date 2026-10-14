package org.vander.spotifyclient.data.remote.dto

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.vander.core.dto.SavedAlbumsResponseDto

/**
 * Parses the `GET /me/albums` payload from Spotify's own reference, exactly as documented, to
 * pin the DTO against it. Same setup as [RecentlyPlayedDtoTest].
 */
class SavedAlbumsDtoTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `the documented payload parses`() {
        val dto = json.decodeFromString<SavedAlbumsResponseDto>(REFERENCE_PAYLOAD)

        assertEquals(4, dto.total)
        assertEquals(1, dto.items.size)
        assertEquals("string", dto.items.single().addedAt)
    }

    @Test
    fun `a saved album carries its full album and its first page of tracks`() {
        val album =
            json
                .decodeFromString<SavedAlbumsResponseDto>(REFERENCE_PAYLOAD)
                .items
                .single()
                .album

        assertEquals("2up3OPMp9Tb4dAKM2erWXQ", album.id)
        assertEquals("market", album.restrictions?.reason)
        assertEquals("string", album.externalIds.upc)
        assertTrue(album.genres.isEmpty())
        assertEquals(1, album.tracks.items.size)
        assertEquals("string", album.copyrights.single().text)
    }

    @Test
    fun `a relinked track keeps the original it was linked from`() {
        // linked_from is an object holding external_urls, not a flat map of strings.
        val track =
            json
                .decodeFromString<SavedAlbumsResponseDto>(REFERENCE_PAYLOAD)
                .items
                .single()
                .album
                .tracks
                .items
                .single()

        assertEquals("string", track.linkedFrom?.externalUrls?.spotify)
    }

    @Test
    fun `a track that was not relinked has no linked_from`() {
        val body = REFERENCE_PAYLOAD.replace(Regex(""""linked_from": \{[^}]*\{[^}]*\}[^}]*\},"""), "")

        val track =
            json
                .decodeFromString<SavedAlbumsResponseDto>(body)
                .items
                .single()
                .album
                .tracks
                .items
                .single()

        assertNull(track.linkedFrom)
    }

    private companion object {
        val REFERENCE_PAYLOAD =
            """
            {
              "href": "https://api.spotify.com/v1/me/shows?offset=0&limit=20",
              "limit": 20,
              "next": "https://api.spotify.com/v1/me/shows?offset=1&limit=1",
              "offset": 0,
              "previous": "https://api.spotify.com/v1/me/shows?offset=1&limit=1",
              "total": 4,
              "items": [
                {
                  "added_at": "string",
                  "album": {
                    "album_type": "compilation",
                    "total_tracks": 9,
                    "available_markets": ["CA", "BR", "IT"],
                    "external_urls": { "spotify": "string" },
                    "href": "string",
                    "id": "2up3OPMp9Tb4dAKM2erWXQ",
                    "images": [
                      {
                        "url": "https://i.scdn.co/image/ab67616d00001e02ff9ca10b55ce82ae553c8228",
                        "height": 300,
                        "width": 300
                      }
                    ],
                    "name": "string",
                    "release_date": "1981-12",
                    "release_date_precision": "year",
                    "restrictions": { "reason": "market" },
                    "type": "album",
                    "uri": "spotify:album:2up3OPMp9Tb4dAKM2erWXQ",
                    "artists": [
                      {
                        "external_urls": { "spotify": "string" },
                        "href": "string",
                        "id": "string",
                        "name": "string",
                        "type": "artist",
                        "uri": "string"
                      }
                    ],
                    "tracks": {
                      "href": "https://api.spotify.com/v1/me/shows?offset=0&limit=20",
                      "limit": 20,
                      "next": "https://api.spotify.com/v1/me/shows?offset=1&limit=1",
                      "offset": 0,
                      "previous": "https://api.spotify.com/v1/me/shows?offset=1&limit=1",
                      "total": 4,
                      "items": [
                        {
                          "artists": [
                            {
                              "external_urls": { "spotify": "string" },
                              "href": "string",
                              "id": "string",
                              "name": "string",
                              "type": "artist",
                              "uri": "string"
                            }
                          ],
                          "available_markets": ["string"],
                          "disc_number": 0,
                          "duration_ms": 0,
                          "explicit": false,
                          "external_urls": { "spotify": "string" },
                          "href": "string",
                          "id": "string",
                          "is_playable": false,
                          "linked_from": {
                            "external_urls": { "spotify": "string" },
                            "href": "string",
                            "id": "string",
                            "type": "string",
                            "uri": "string"
                          },
                          "restrictions": { "reason": "string" },
                          "name": "string",
                          "preview_url": "string",
                          "track_number": 0,
                          "type": "string",
                          "uri": "string",
                          "is_local": false
                        }
                      ]
                    },
                    "copyrights": [{ "text": "string", "type": "string" }],
                    "external_ids": { "isrc": "string", "ean": "string", "upc": "string" },
                    "genres": [],
                    "label": "string",
                    "popularity": 0
                  }
                }
              ]
            }
            """.trimIndent()
    }
}
