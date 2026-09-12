package org.vander.spotifyclient.data.playlist.mapper

import org.vander.core.domain.data.Playlist
import org.vander.core.domain.data.PlaylistCollection
import org.vander.core.dto.SpotifyPlaylistDto
import org.vander.core.dto.SpotifyPlaylistsResponseDto

/**
 * Reduces the API's playlist page to the domain model, dropping the pagination fields —
 * only the first page is ever requested today.
 */
fun SpotifyPlaylistsResponseDto.toDomain(): PlaylistCollection =
    PlaylistCollection(
        items = items.map { it.toDomain() },
    )

fun SpotifyPlaylistDto.toDomain(): Playlist =
    Playlist(
        id = id,
        name = name,
        coverUrl = images?.firstOrNull()?.url.orEmpty(),
    )
