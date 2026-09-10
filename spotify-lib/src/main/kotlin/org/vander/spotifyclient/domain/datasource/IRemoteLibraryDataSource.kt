package org.vander.spotifyclient.domain.datasource

interface IRemoteLibraryDataSource {
    suspend fun fetchIsTrackSaved(trackId: String): Result<Boolean>

    suspend fun saveTrack(trackId: String): Result<Unit>

    suspend fun removeTrack(trackId: String): Result<Unit>
}
