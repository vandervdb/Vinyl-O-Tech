package org.vander.spotifyclient.data.repository

import org.vander.spotifyclient.domain.datasource.IRemoteLibraryDataSource
import org.vander.spotifyclient.domain.repository.LibraryRepository
import javax.inject.Inject

/**
 * [LibraryRepository] that forwards straight to the remote data source — no cache, no state.
 *
 * Kept as a separate class even though it adds nothing today: it is the seam where a cache
 * or an optimistic update would go, and it keeps the use cases from depending on a data source.
 */
class SpotifyLibraryRepository
    @Inject
    constructor(
        private val api: IRemoteLibraryDataSource,
    ) : LibraryRepository {
        override suspend fun isTrackSaved(trackId: String): Result<Boolean> = api.fetchIsTrackSaved(trackId)

        override suspend fun saveTrack(trackId: String): Result<Unit> = api.saveTrack(trackId)

        override suspend fun removeTrack(trackId: String): Result<Unit> = api.removeTrack(trackId)
    }
