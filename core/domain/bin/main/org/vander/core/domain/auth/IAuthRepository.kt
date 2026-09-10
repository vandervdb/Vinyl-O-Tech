package org.vander.core.domain.auth

interface IAuthRepository {
    suspend fun storeAccessToken(token: String): Result<Unit>

    suspend fun getAccessToken(): Result<String>

    suspend fun clearAccessToken(): Result<Unit>
}
