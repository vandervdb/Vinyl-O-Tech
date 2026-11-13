package org.vander.spotifyclient.domain.album

import kotlinx.coroutines.flow.Flow
import org.vander.core.domain.data.SavedAlbumObject

internal interface AlbumRepository {
    val savedAlbums: Flow<List<SavedAlbumObject>>

    suspend fun refreshSavedAlbums(): Result<Unit>
}
