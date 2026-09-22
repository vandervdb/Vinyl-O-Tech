package org.vander.core.security.api

/**
 * Encrypted storage for the Spotify OAuth tokens.
 *
 * No consumer yet: tokens are currently stored unencrypted by `spotify-lib`'s
 * `DataStoreManager`, which does not implement this interface.
 *
 * [get] never throws — every outcome is a [StoredTokensResult] branch. Writes return a
 * [Result] instead, because a failed write must not look like a successful one; that is the
 * same choice `spotify-lib`'s `IDataStoreManager` already made for DataStore writes.
 *
 * Expiry is not checked here: the caller compares [StoredTokens.expiresAt] itself.
 */
interface SecureTokenStorage {
    suspend fun save(
        accessToken: String,
        refreshToken: String,
        expiresAt: Long,
    ): Result<Unit>

    suspend fun get(): StoredTokensResult

    suspend fun clear(): Result<Unit>
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

/**
 * Outcome of [SecureTokenStorage.get].
 *
 * A sealed class rather than a nullable [StoredTokens]: `null` would conflate "no session
 * yet" with "a session is stored but cannot be read", which call for opposite reactions. The
 * two failures are kept apart for the same reason — with a single failure branch the caller
 * would have to type-check a `Throwable` to know whether clearing the session is the answer.
 */
sealed class StoredTokensResult {
    data class Found(
        val tokens: StoredTokens,
    ) : StoredTokensResult()

    /** Nothing stored, or the session was cleared — the normal first-launch case. */
    object Empty : StoredTokensResult()

    /**
     * Transient: the store could not be read at all. Retrying is meaningful, clearing is not
     * — the ciphertext is presumably still valid.
     */
    data class ReadFailed(
        val cause: Throwable,
    ) : StoredTokensResult()

    /**
     * Permanent: the stored bytes no longer decrypt — a tampered file, or a master key the
     * Android Keystore no longer holds. The session is unrecoverable, so the caller is
     * expected to [SecureTokenStorage.clear] it and re-authenticate.
     */
    data class DecryptionFailed(
        val cause: Throwable,
    ) : StoredTokensResult()
}
