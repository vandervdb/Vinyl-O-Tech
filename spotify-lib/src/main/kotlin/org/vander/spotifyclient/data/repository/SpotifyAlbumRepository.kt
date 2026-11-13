package org.vander.spotifyclient.data.repository

import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.vander.core.domain.data.SavedAlbumObject
import org.vander.spotifyclient.data.album.mapper.toDomain
import org.vander.spotifyclient.data.remote.datasource.RemoteAlbumDataSource
import org.vander.spotifyclient.domain.album.AlbumRepository
import javax.inject.Inject

internal class SpotifyAlbumRepository
    @Inject
    constructor(
        private val api: RemoteAlbumDataSource,
    ) : AlbumRepository {
        private val _savedAlbums = MutableStateFlow<List<SavedAlbumObject>>(emptyList())
        override val savedAlbums: StateFlow<List<SavedAlbumObject>> = _savedAlbums.asStateFlow()

        override suspend fun refreshSavedAlbums() =
            try {
                val dto = api.fetchSavedAlbums().getOrThrow()
                _savedAlbums.update { dto.toDomain().items }
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
    }
