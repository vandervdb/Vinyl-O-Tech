package org.vander.spotifyclient.domain.repository

import org.vander.core.domain.library.LibraryRepository

/**
 * In-memory library. [savedIds] is what `isTrackSaved` answers from; [failWith], when set, makes
 * every call fail with it. Every call is recorded, so a test can count the lookups.
 */
class FakeLibraryRepository : LibraryRepository {
    val savedIds = mutableSetOf<String>()

    var failWith: Throwable? = null

    val lookups = mutableListOf<String>()

    val saved = mutableListOf<String>()

    val removed = mutableListOf<String>()

    override suspend fun isTrackSaved(trackId: String): Result<Boolean> {
        lookups += trackId
        return failWith?.let { Result.failure(it) } ?: Result.success(trackId in savedIds)
    }

    override suspend fun saveTrack(trackId: String): Result<Unit> {
        saved += trackId
        return failWith?.let { Result.failure(it) } ?: Result.success(Unit).also { savedIds += trackId }
    }

    override suspend fun removeTrack(trackId: String): Result<Unit> {
        removed += trackId
        return failWith?.let { Result.failure(it) } ?: Result.success(Unit).also { savedIds -= trackId }
    }
}
