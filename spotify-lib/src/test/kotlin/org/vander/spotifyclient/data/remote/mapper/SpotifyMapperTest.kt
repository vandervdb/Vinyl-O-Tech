package org.vander.spotifyclient.data.remote.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.vander.core.dto.AlbumDto
import org.vander.core.dto.ArtistDto
import org.vander.core.dto.CurrentlyPlayingWithQueueDto
import org.vander.core.dto.ExplicitContentDto
import org.vander.core.dto.ExternalIdsDto
import org.vander.core.dto.ExternalUrlDto
import org.vander.core.dto.ExternalUrlsDto
import org.vander.core.dto.FollowersDto
import org.vander.core.dto.ImageDto
import org.vander.core.dto.TrackDto
import org.vander.core.dto.UserDto

/**
 * The domain models here (`Track`, `Queue`, `Album`, `Artist`) are plain `class`, not
 * `data class`, so they have no structural equality — every assertion compares fields
 * one by one rather than whole objects.
 */
class SpotifyMapperTest {
    // --- Track

    @Test
    fun `a track keeps the fields the app uses`() {
        val domain = trackDto(id = "4cOdK2", name = "Nuits blanches", durationMs = 214_000).toDomain()

        assertEquals("4cOdK2", domain.id)
        assertEquals("Nuits blanches", domain.name)
        assertEquals(214_000, domain.durationMs)
        assertEquals("spotify:track:4cOdK2", domain.uri)
    }

    @Test
    fun `external urls are flattened to the spotify link`() {
        val domain = trackDto(spotifyUrl = "https://open.spotify.com/track/4cOdK2").toDomain()

        assertEquals("https://open.spotify.com/track/4cOdK2", domain.externalUrls)
    }

    @Test
    fun `external ids keep the isrc only`() {
        val domain = trackDto(externalIds = ExternalIdsDto(isrc = "FRX122300001", ean = "42", upc = "43")).toDomain()

        assertEquals("FRX122300001", domain.externalIds)
    }

    @Test
    fun `a missing isrc becomes an empty string, not null`() {
        val domain = trackDto(externalIds = ExternalIdsDto()).toDomain()

        assertEquals("", domain.externalIds)
    }

    @Test
    fun `a track carries its album and its artists mapped too`() {
        val domain =
            trackDto(
                artists = listOf(artistDto(id = "a1", name = "Elia Faure"), artistDto(id = "a2", name = "Nord Nord")),
            ).toDomain()

        assertEquals(listOf("Elia Faure", "Nord Nord"), domain.artists.map { it.name })
        assertEquals(listOf("a1", "a2"), domain.artists.map { it.id })
    }

    // --- Album

    @Test
    fun `an album maps its images and its artists`() {
        val domain =
            albumDto(
                images =
                    listOf(
                        ImageDto(url = "https://i.scdn.co/640", height = 640, width = 640),
                        ImageDto(url = "https://i.scdn.co/64"),
                    ),
                artists = listOf(artistDto(name = "Elia Faure")),
            ).toDomain()

        assertEquals(listOf("https://i.scdn.co/640", "https://i.scdn.co/64"), domain.images.map { it.url })
        assertEquals(640, domain.images.first().height)
        assertEquals(listOf("Elia Faure"), domain.artists.map { it.name })
    }

    @Test
    fun `an image without dimensions keeps them null`() {
        val domain = albumDto(images = listOf(ImageDto(url = "https://i.scdn.co/x"))).toDomain()

        assertNull(domain.images.first().height)
        assertNull(domain.images.first().width)
    }

    // --- Currently playing + queue

    @Test
    fun `a null queue slot becomes an empty track rather than disappearing`() {
        // Positions matter to the MiniPlayer's pager, so a hole is filled, never dropped.
        val dto =
            CurrentlyPlayingWithQueueDto(
                currentlyPlaying = trackDto(id = "current"),
                queue = listOf(trackDto(id = "next"), null, trackDto(id = "later")),
            )

        val domain = dto.toDomain()

        assertEquals(3, domain.queue.tracks.size)
        assertEquals(listOf("next", "", "later"), domain.queue.tracks.map { it.id })
    }

    @Test
    fun `nothing playing maps to a null current track`() {
        val domain = CurrentlyPlayingWithQueueDto(currentlyPlaying = null, queue = emptyList()).toDomain()

        assertNull(domain.currentlyPlaying)
        assertTrue(domain.queue.tracks.isEmpty())
    }

    @Test
    fun `the queue keeps its order`() {
        val dto =
            CurrentlyPlayingWithQueueDto(
                queue = listOf(trackDto(id = "1"), trackDto(id = "2"), trackDto(id = "3")),
            )

        assertEquals(
            listOf("1", "2", "3"),
            dto
                .toDomain()
                .queue.tracks
                .map { it.id },
        )
    }

    // --- User

    @Test
    fun `a user keeps the display name and the first picture`() {
        val domain =
            userDto(
                displayName = "Vander",
                images =
                    listOf(
                        ImageDto(url = "https://i.scdn.co/avatar-big"),
                        ImageDto(url = "https://i.scdn.co/avatar-small"),
                    ),
            ).toDomain()

        assertEquals("Vander", domain.name)
        assertEquals("https://i.scdn.co/avatar-big", domain.imageUrl)
    }

    @Test
    fun `a user without a picture maps to a null image url`() {
        assertNull(userDto(images = emptyList()).toDomain().imageUrl)
    }

    // --- Builders

    private fun artistDto(
        id: String = "artist",
        name: String = "Artist",
    ) = ArtistDto(
        externalUrls = ExternalUrlDto(spotify = "https://open.spotify.com/artist/$id"),
        href = "https://api.spotify.com/v1/artists/$id",
        id = id,
        name = name,
        type = "artist",
        uri = "spotify:artist:$id",
    )

    private fun albumDto(
        id: String = "album",
        images: List<ImageDto> = listOf(ImageDto(url = "https://i.scdn.co/cover")),
        artists: List<ArtistDto> = listOf(artistDto()),
    ) = AlbumDto(
        albumType = "album",
        totalTracks = 11,
        availableMarkets = listOf("FR"),
        externalUrls = ExternalUrlDto(spotify = "https://open.spotify.com/album/$id"),
        href = "https://api.spotify.com/v1/albums/$id",
        id = id,
        images = images,
        name = "Nuits blanches",
        releaseDate = "2023-04-14",
        releaseDatePrecision = "day",
        type = "album",
        uri = "spotify:album:$id",
        artists = artists,
    )

    private fun trackDto(
        id: String = "track",
        name: String = "Track",
        durationMs: Int = 180_000,
        spotifyUrl: String = "https://open.spotify.com/track/$id",
        externalIds: ExternalIdsDto = ExternalIdsDto(isrc = "FRX000000001"),
        artists: List<ArtistDto> = listOf(artistDto()),
    ) = TrackDto(
        album = albumDto(),
        artists = artists,
        availableMarkets = listOf("FR"),
        discNumber = 1,
        durationMs = durationMs,
        explicit = false,
        externalIds = externalIds,
        externalUrls = ExternalUrlDto(spotify = spotifyUrl),
        href = "https://api.spotify.com/v1/tracks/$id",
        id = id,
        name = name,
        popularity = 42,
        previewUrl = null,
        trackNumber = 3,
        type = "track",
        uri = "spotify:track:$id",
        isLocal = false,
    )

    private fun userDto(
        displayName: String = "Vander",
        images: List<ImageDto> = listOf(ImageDto(url = "https://i.scdn.co/avatar")),
    ) = UserDto(
        country = "FR",
        displayName = displayName,
        email = "vander@example.org",
        explicitContent = ExplicitContentDto(filterEnabled = false, filterLocked = false),
        externalUrls = ExternalUrlsDto(spotify = "https://open.spotify.com/user/vander"),
        followers = FollowersDto(total = 7),
        href = "https://api.spotify.com/v1/users/vander",
        id = "vander",
        images = images,
        product = "premium",
        type = "user",
        uri = "spotify:user:vander",
    )
}
