package org.vander.spotifyclient.domain.auth

import kotlinx.coroutines.flow.Flow

interface IDataStoreManager {
    val accessTokenFlow: Flow<String?>

    suspend fun saveAccessToken(token: String): Result<Unit>

    suspend fun getAccessToken(): Result<String>

    suspend fun clearAccessToken(): Result<Unit>

    suspend fun saveRefreshToken(token: String): Result<Unit>

    suspend fun getRefreshToken(): Result<String>

    suspend fun clearRefreshToken(): Result<Unit>
}
