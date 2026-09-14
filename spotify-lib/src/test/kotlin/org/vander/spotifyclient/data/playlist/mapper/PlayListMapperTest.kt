package org.vander.spotifyclient.data.playlist.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.vander.core.dto.ExternalUrlsDto
import org.vander.core.dto.SpotifyImageDto
import org.vander.core.dto.SpotifyOwnerDto
import org.vander.core.dto.SpotifyPlaylistDto
import org.vander.core.dto.SpotifyPlaylistsResponseDto
import org.vander.core.dto.SpotifyTracksDto

class PlayListMapperTest {
    @Test
    fun `a playlist keeps its id, its name and its first cover`() {
        val domain =
            playlistDto(
                id = "37i9dQZF1DXcBWIGoYBM5M",
                name = "Sillons",
                images =
                    listOf(
                        SpotifyImageDto(url = "https://i.scdn.co/large"),
                        SpotifyImageDto(url = "https://i.scdn.co/small"),
                    ),
            ).toDomain()

        assertEquals("37i9dQZF1DXcBWIGoYBM5M", domain.id)
        assertEquals("Sillons", domain.name)
        assertEquals("https://i.scdn.co/large", domain.coverUrl)
    }

    @Test
    fun `a playlist without images maps to an empty cover url`() {
        // The API omits `images` entirely for a coverless playlist, hence the nullable list
        // on the DTO. The grid needs a non-null String, so the absence collapses to "".
        assertEquals("", playlistDto(images = null).toDomain().coverUrl)
    }

    @Test
    fun `an empty image list maps to an empty cover url too`() {
        // Distinct wire shape from the null above, same domain outcome.
        assertEquals("", playlistDto(images = emptyList()).toDomain().coverUrl)
    }

    @Test
    fun `a page maps every item and keeps their order`() {
        val page =
            SpotifyPlaylistsResponseDto(
                href = "https://api.spotify.com/v1/me/playlists",
                limit = 20,
                offset = 0,
                total = 3,
                items =
                    listOf(
                        playlistDto(id = "a", name = "Sillons"),
                        playlistDto(id = "b", name = "Marée haute"),
                        playlistDto(id = "c", name = "Braise"),
                    ),
            )

        val domain = page.toDomain()

        assertEquals(listOf("a", "b", "c"), domain.items.map { it.id })
        assertEquals(listOf("Sillons", "Marée haute", "Braise"), domain.items.map { it.name })
    }

    @Test
    fun `an empty page maps to an empty collection`() {
        val page =
            SpotifyPlaylistsResponseDto(
                href = "https://api.spotify.com/v1/me/playlists",
                limit = 20,
                offset = 0,
                total = 0,
                items = emptyList(),
            )

        assertTrue(page.toDomain().items.isEmpty())
    }

    @Test
    fun `pagination fields are dropped, not carried into the domain`() {
        // PlaylistCollection has no `next`/`total`: only the first page is ever requested.
        // This test exists so that adding pagination later is a deliberate change here.
        val page =
            SpotifyPlaylistsResponseDto(
                href = "https://api.spotify.com/v1/me/playlists",
                limit = 20,
                next = "https://api.spotify.com/v1/me/playlists?offset=20",
                offset = 0,
                total = 243,
                items = listOf(playlistDto()),
            )

        assertEquals(1, page.toDomain().items.size)
    }

    private fun playlistDto(
        id: String = "id",
        name: String = "name",
        images: List<SpotifyImageDto>? = listOf(SpotifyImageDto(url = "https://i.scdn.co/cover")),
    ) = SpotifyPlaylistDto(
        collaborative = false,
        description = "",
        externalUrls = ExternalUrlsDto(spotify = "https://open.spotify.com/playlist/$id"),
        href = "https://api.spotify.com/v1/playlists/$id",
        id = id,
        images = images,
        name = name,
        owner =
            SpotifyOwnerDto(
                externalUrls = ExternalUrlsDto(spotify = "https://open.spotify.com/user/vander"),
                href = "https://api.spotify.com/v1/users/vander",
                id = "vander",
                type = "user",
                uri = "spotify:user:vander",
                displayName = "Vander",
            ),
        public = null,
        snapshotId = "snapshot",
        tracks = SpotifyTracksDto(href = "https://api.spotify.com/v1/playlists/$id/tracks", total = 11),
        type = "playlist",
        uri = "spotify:playlist:$id",
    )
}
