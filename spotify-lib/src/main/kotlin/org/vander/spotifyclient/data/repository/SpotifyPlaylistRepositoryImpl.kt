package org.vander.spotifyclient.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.vander.core.domain.data.PlaylistCollection
import org.vander.spotifyclient.data.playlist.mapper.toDomain
import org.vander.spotifyclient.domain.datasource.IRemotePlaylistDataSource
import org.vander.spotifyclient.domain.repository.SpotifyPlaylistRepository
import javax.inject.Inject

/**
 * Fetches the playlists, maps the DTO to the domain model and caches the result in memory.
 *
 * `getOrThrow()` inside a `try` is how the data source's [Result] is re-wrapped into this
 * one: a mapping failure and a network failure then come out the same way.
 */
class SpotifyPlaylistRepositoryImpl
    @Inject
    constructor(
        private val api: IRemotePlaylistDataSource,
    ) : SpotifyPlaylistRepository {
        private val _playlists = MutableStateFlow<PlaylistCollection?>(null)
        override val playlists: StateFlow<PlaylistCollection?> = _playlists.asStateFlow()

        override suspend fun getUserPlaylists(): Result<PlaylistCollection> =
            try {
                val dto = api.fetchUserPlaylists().getOrThrow()
                val result = dto.toDomain()
                _playlists.update { result }
                Result.success(result)
            } catch (e: Exception) {
                Result.failure(e)
            }
    }
