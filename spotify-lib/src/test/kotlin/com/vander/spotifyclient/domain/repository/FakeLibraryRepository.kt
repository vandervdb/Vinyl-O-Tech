package com.vander.spotifyclient.domain.repository

import org.vander.spotifyclient.domain.repository.LibraryRepository

class FakeLibraryRepository : LibraryRepository {
    override suspend fun isTrackSaved(trackId: String): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun saveTrack(trackId: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun removeTrack(trackId: String): Result<Unit> {
        TODO("Not yet implemented")
    }
}
