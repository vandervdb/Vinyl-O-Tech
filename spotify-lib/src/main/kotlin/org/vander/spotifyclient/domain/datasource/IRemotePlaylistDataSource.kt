package org.vander.spotifyclient.domain.datasource

import org.vander.core.dto.SpotifyPlaylistsResponseDto

fun interface IRemotePlaylistDataSource {
    suspend fun fetchUserPlaylists(): Result<SpotifyPlaylistsResponseDto>
}
