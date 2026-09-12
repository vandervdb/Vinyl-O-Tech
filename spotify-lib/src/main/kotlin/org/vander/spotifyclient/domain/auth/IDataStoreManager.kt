package org.vander.spotifyclient.domain.auth

import kotlinx.coroutines.flow.Flow

/**
 * Reads and writes both OAuth tokens in DataStore.
 *
 * Wider than [org.vander.core.domain.auth.IAuthRepository], which exposes the access token
 * only: this is the storage-facing contract, the domain one is what the rest of the app sees.
 *
 * Every write returns a [Result] — DataStore signals an I/O failure through an exception
 * that must not escape this boundary.
 */
interface IDataStoreManager {
    val accessTokenFlow: Flow<String?>

    suspend fun saveAccessToken(token: String): Result<Unit>

    /** @return a success holding an empty string when nothing was stored; only an I/O error fails. */
    suspend fun getAccessToken(): Result<String>

    suspend fun clearAccessToken(): Result<Unit>

    /** Not implemented — the current implementation throws. */
    suspend fun saveRefreshToken(token: String): Result<Unit>

    /** Not implemented — the current implementation throws. */
    suspend fun getRefreshToken(): Result<String>

    /** Not implemented — the current implementation throws. */
    suspend fun clearRefreshToken(): Result<Unit>
}
