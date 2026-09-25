package org.vander.spotifyclient.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.vander.core.domain.data.PlaylistCollection
import org.vander.core.domain.playlist.PlaylistRepository
import org.vander.spotifyclient.data.playlist.mapper.toDomain
import org.vander.spotifyclient.domain.datasource.IRemotePlaylistDataSource
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Fetches the playlists, maps the DTO to the domain model and caches the result in memory.
 *
 * `getOrThrow()` inside a `try` is how the data source's [Result] is re-wrapped into this
 * one: a mapping failure and a network failure then come out the same way.
 */
class SpotifyPlaylistRepository
    @Inject
    constructor(
        private val api: IRemotePlaylistDataSource,
    ) : PlaylistRepository {
        private val _playlists = MutableStateFlow(PlaylistCollection.empty())
        override val playlists: StateFlow<PlaylistCollection> = _playlists.asStateFlow()

        override suspend fun refresh() =
            try {
                val dto = api.fetchUserPlaylists().getOrThrow()
                val result = dto.toDomain()
                _playlists.update { result }
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
    }
