package org.vander.core.security.api

/**
 * Encrypted storage for the Spotify OAuth tokens.
 *
 * No implementation exists yet. Tokens are currently stored unencrypted by
 * `spotify-lib`'s `DataStoreManager`, which does not implement this interface.
 *
 * [get] returns `null` when no session was ever stored; expiry is not checked here, the
 * caller compares [StoredTokens.expiresAt] itself.
 */
interface SecureTokenStorage {
    suspend fun save(
        accessToken: String,
        refreshToken: String,
        expiresAt: Long,
    )

    suspend fun get(): StoredTokens?

    suspend fun clear()
}

/**
 * A decrypted token pair.
 *
 * @property expiresAt absolute expiry as an epoch timestamp — unlike the API's
 *   `expires_in`, which is a duration in seconds from the moment of issue.
 */
data class StoredTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long,
)
