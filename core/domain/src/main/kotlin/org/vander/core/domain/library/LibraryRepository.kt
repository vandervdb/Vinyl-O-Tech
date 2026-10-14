package org.vander.core.domain.library

/**
 * The user's saved-tracks library, on the Web API side — the App Remote knows nothing
 * about it.
 *
 * @see org.vander.core.domain.state.DomainPlayerState.isTrackSaved for how the answer is
 *   merged back into the player state.
 */
interface LibraryRepository {
    suspend fun isTrackSaved(trackId: String): Result<Boolean>

    suspend fun saveTrack(trackId: String): Result<Unit>

    suspend fun removeTrack(trackId: String): Result<Unit>
}
