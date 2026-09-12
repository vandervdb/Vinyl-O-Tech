package org.vander.spotifyclient.domain.player

import kotlinx.coroutines.flow.Flow

/**
 * Unused duplicate of [org.vander.spotifyclient.domain.auth.IDataStoreManager], narrowed to
 * the access token. No file imports this one — the `domain.auth` version is the live contract.
 */
interface IDataStoreManager {
    val accessTokenFlow: Flow<String?>

    suspend fun saveAccessToken(token: String): Result<Unit>

    suspend fun getAccessToken(): Result<String>

    suspend fun clearAccessToken(): Result<Unit>
}
