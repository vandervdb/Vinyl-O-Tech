package org.vander.spotifyclient.domain.datasource

/** Raw `me/tracks` calls; the repository above it adds no caching today. */
interface IRemoteLibraryDataSource {
    /**
     * @return the first element of the API's answer, which is an array aligned on the ids sent;
     *   a single id is queried here.
     */
    suspend fun fetchIsTrackSaved(trackId: String): Result<Boolean>

    suspend fun saveTrack(trackId: String): Result<Unit>

    suspend fun removeTrack(trackId: String): Result<Unit>
}
