package org.vander.spotifyclient.fixtures

import org.vander.core.dto.AlbumDto
import org.vander.core.dto.ArtistDto
import org.vander.core.dto.CurrentlyPlayingWithQueueDto
import org.vander.core.dto.ExplicitContentDto
import org.vander.core.dto.ExternalIdsDto
import org.vander.core.dto.ExternalUrlDto
import org.vander.core.dto.ExternalUrlsDto
import org.vander.core.dto.FollowersDto
import org.vander.core.dto.ImageDto
import org.vander.core.dto.SpotifyImageDto
import org.vander.core.dto.SpotifyOwnerDto
import org.vander.core.dto.SpotifyPlaylistDto
import org.vander.core.dto.SpotifyPlaylistsResponseDto
import org.vander.core.dto.SpotifyTracksDto
import org.vander.core.dto.TrackDto
import org.vander.core.dto.UserDto

/**
 * Valid wire payloads for tests that need *a* DTO rather than a particular one — the
 * repositories, mostly, which only forward what the data source returns.
 *
 * A test about the mapping itself builds its own variations instead: what matters there is
 * the odd shape (a null image list, a hole in the queue), not a plausible payload.
 */
fun artistDto(
    id: String = "artist",
    name: String = "Elia Faure",
) = ArtistDto(
    externalUrls = ExternalUrlDto(spotify = "https://open.spotify.com/artist/$id"),
    href = "https://api.spotify.com/v1/artists/$id",
    id = id,
    name = name,
    type = "artist",
    uri = "spotify:artist:$id",
)

fun albumDto(id: String = "album") =
    AlbumDto(
        albumType = "album",
        totalTracks = 11,
        availableMarkets = listOf("FR"),
        externalUrls = ExternalUrlDto(spotify = "https://open.spotify.com/album/$id"),
        href = "https://api.spotify.com/v1/albums/$id",
        id = id,
        images = listOf(ImageDto(url = "https://i.scdn.co/cover", height = 640, width = 640)),
        name = "Nuits blanches",
        releaseDate = "2023-04-14",
        releaseDatePrecision = "day",
        type = "album",
        uri = "spotify:album:$id",
        artists = listOf(artistDto()),
    )

fun trackDto(
    id: String = "track",
    name: String = "Nuits blanches",
) = TrackDto(
    album = albumDto(),
    artists = listOf(artistDto()),
    availableMarkets = listOf("FR"),
    discNumber = 1,
    durationMs = 214_000,
    explicit = false,
    externalIds = ExternalIdsDto(isrc = "FRX122300001"),
    externalUrls = ExternalUrlDto(spotify = "https://open.spotify.com/track/$id"),
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

fun playlistDto(
    id: String = "37i9dQZF1DXcBWIGoYBM5M",
    name: String = "Sillons",
) = SpotifyPlaylistDto(
    collaborative = false,
    description = "",
    externalUrls = ExternalUrlsDto(spotify = "https://open.spotify.com/playlist/$id"),
    href = "https://api.spotify.com/v1/playlists/$id",
    id = id,
    images = listOf(SpotifyImageDto(url = "https://i.scdn.co/playlist-cover")),
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

fun playlistPageDto(items: List<SpotifyPlaylistDto> = listOf(playlistDto())) =
    SpotifyPlaylistsResponseDto(
        href = "https://api.spotify.com/v1/me/playlists",
        limit = 20,
        offset = 0,
        total = items.size,
        items = items,
    )

fun queueDto(
    currentlyPlaying: TrackDto? = trackDto(id = "current"),
    queue: List<TrackDto?> = listOf(trackDto(id = "next")),
) = CurrentlyPlayingWithQueueDto(currentlyPlaying = currentlyPlaying, queue = queue)

fun userDto(displayName: String = "Vander") =
    UserDto(
        country = "FR",
        displayName = displayName,
        email = "vander@example.org",
        explicitContent = ExplicitContentDto(filterEnabled = false, filterLocked = false),
        externalUrls = ExternalUrlsDto(spotify = "https://open.spotify.com/user/vander"),
        followers = FollowersDto(total = 7),
        href = "https://api.spotify.com/v1/users/vander",
        id = "vander",
        images = listOf(ImageDto(url = "https://i.scdn.co/avatar")),
        product = "premium",
        type = "user",
        uri = "spotify:user:vander",
    )
