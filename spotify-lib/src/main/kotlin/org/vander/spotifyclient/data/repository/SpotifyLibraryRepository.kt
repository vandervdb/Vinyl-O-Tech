package org.vander.spotifyclient.data.repository

import org.vander.spotifyclient.domain.datasource.IRemoteLibraryDataSource
import org.vander.spotifyclient.domain.repository.LibraryRepository
import javax.inject.Inject

class SpotifyLibraryRepository
    @Inject
    constructor(
        private val api: IRemoteLibraryDataSource,
    ) : LibraryRepository {
        override suspend fun isTrackSaved(trackId: String): Result<Boolean> = api.fetchIsTrackSaved(trackId)

        override suspend fun saveTrack(trackId: String): Result<Unit> = api.saveTrack(trackId)

        override suspend fun removeTrack(trackId: String): Result<Unit> = api.removeTrack(trackId)
    }
