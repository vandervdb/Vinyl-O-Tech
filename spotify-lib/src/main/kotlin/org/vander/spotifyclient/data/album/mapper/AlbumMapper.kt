package org.vander.spotifyclient.data.album.mapper

import org.vander.core.domain.data.Album
import org.vander.core.domain.data.SavedAlbumObject
import org.vander.core.domain.data.SavedAlbumsCollection
import org.vander.core.dto.FullAlbumDto
import org.vander.core.dto.SavedAlbumDto
import org.vander.core.dto.SavedAlbumsResponseDto
import org.vander.spotifyclient.data.remote.mapper.toDomain

fun SavedAlbumsResponseDto.toDomain(): SavedAlbumsCollection =
    SavedAlbumsCollection(
        items = items.map { it.toDomain() },
    )

fun SavedAlbumDto.toDomain(): SavedAlbumObject =
    SavedAlbumObject(
        addedAt = addedAt,
        album = album.toDomain(),
    )

fun FullAlbumDto.toDomain(): Album =
    Album(
        albumType = albumType,
        totalTracks = totalTracks,
        availableMarkets = availableMarkets,
        externalUrls = externalUrls.spotify,
        href = href,
        id = id,
        images = images.map { it.toDomain() },
        name = name,
        releaseDate = releaseDate,
        type = type,
        uri = uri,
        artists = artists.map { it.toDomain() },
    )
