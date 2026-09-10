package org.vander.core.security.api

interface SecureTokenStorage {
    suspend fun save(
        accessToken: String,
        refreshToken: String,
        expiresAt: Long,
    )

    suspend fun get(): StoredTokens?

    suspend fun clear()
}

data class StoredTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long,
)
