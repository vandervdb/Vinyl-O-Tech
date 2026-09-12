package org.vander.spotifyclient.domain.datasource

import org.vander.core.dto.SpotifyPlaylistsResponseDto

/**
 * Raw `me/playlists` call, returning the DTO untouched — mapping to the domain model
 * happens in the repository.
 *
 * A `fun interface`: one abstract method, so a test can pass a lambda instead of a class.
 */
fun interface IRemotePlaylistDataSource {
    suspend fun fetchUserPlaylists(): Result<SpotifyPlaylistsResponseDto>
}
