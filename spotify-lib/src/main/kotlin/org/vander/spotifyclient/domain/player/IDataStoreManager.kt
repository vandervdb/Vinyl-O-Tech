package org.vander.spotifyclient.domain.player

import kotlinx.coroutines.flow.Flow

interface IDataStoreManager {
    val accessTokenFlow: Flow<String?>

    suspend fun saveAccessToken(token: String): Result<Unit>

    suspend fun getAccessToken(): Result<String>

    suspend fun clearAccessToken(): Result<Unit>
}
