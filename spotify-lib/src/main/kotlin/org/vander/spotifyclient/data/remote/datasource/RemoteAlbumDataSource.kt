package org.vander.spotifyclient.data.remote.datasource

import org.vander.core.dto.SavedAlbumsResponseDto

internal interface RemoteAlbumDataSource {
    suspend fun fetchSavedAlbums(): Result<SavedAlbumsResponseDto>
}
