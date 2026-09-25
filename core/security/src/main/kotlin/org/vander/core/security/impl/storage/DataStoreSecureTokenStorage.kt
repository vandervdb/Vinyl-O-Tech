package org.vander.core.security.impl.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import org.vander.core.logger.Logger
import org.vander.core.security.api.CryptoEngine
import org.vander.core.security.api.SecureTokenStorage
import org.vander.core.security.api.StoredTokens
import org.vander.core.security.api.StoredTokensResult
import org.vander.core.security.di.SecurityDataStore
import java.io.IOException
import java.security.GeneralSecurityException
import java.util.Base64
import javax.inject.Inject

/**
 * [SecureTokenStorage] over the `security_store` DataStore.
 *
 * Both tokens are encrypted by [CryptoEngine] and stored Base64-encoded — Preferences
 * DataStore holds primitives only, the same constraint [DataStoreKeysetRepository] works
 * around. `expiresAt` stays in clear: it is not a secret, and keeping it readable lets a
 * caller test expiry without a decryption, so without a working Keystore.
 *
 * Each token carries its own associated data, so a ciphertext cannot be moved from one slot
 * to the other — decryption fails when the AAD differs. Without it, anything able to write to
 * the file could swap the two values and have them decrypt fine.
 *
 * Nothing here throws: reads map every failure onto a [StoredTokensResult] branch, writes onto
 * a failed [Result]. Reads never write either — an [IOException] is transient, and clearing a
 * session because the disk hiccuped would destroy a refresh token that was still valid.
 */
class DataStoreSecureTokenStorage
    @Inject
    constructor(
        @param:SecurityDataStore private val dataStore: DataStore<Preferences>,
        private val cryptoEngine: CryptoEngine,
        private val logger: Logger,
    ) : SecureTokenStorage {
        companion object {
            private const val TAG = "DataStoreSecureTokenStorage"
            private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token_cipher")
            private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token_cipher")
            private val EXPIRES_AT_KEY = longPreferencesKey("expires_at")
            private val ACCESS_TOKEN_AAD = "spotify.access_token".toByteArray()
            private val REFRESH_TOKEN_AAD = "spotify.refresh_token".toByteArray()
        }

        override suspend fun save(
            accessToken: String,
            refreshToken: String,
            expiresAt: Long,
        ): Result<Unit> =
            try {
                // Encrypt before opening the transaction: encrypt() may read the keyset from
                // disk, and edit() holds a write lock on the file for the whole block.
                val encryptedAccessToken = cryptoEngine.encrypt(accessToken, ACCESS_TOKEN_AAD).encodeBase64()
                val encryptedRefreshToken = cryptoEngine.encrypt(refreshToken, REFRESH_TOKEN_AAD).encodeBase64()

                // One edit for the three values: two would leave a window where the stored
                // access token no longer matches the stored refresh token.
                dataStore.edit { preferences ->
                    preferences[ACCESS_TOKEN_KEY] = encryptedAccessToken
                    preferences[REFRESH_TOKEN_KEY] = encryptedRefreshToken
                    preferences[EXPIRES_AT_KEY] = expiresAt
                }
                Result.success(Unit)
            } catch (e: GeneralSecurityException) {
                logger.e(TAG, "Failed to encrypt the tokens", e)
                Result.failure(e)
            } catch (e: IOException) {
                logger.e(TAG, "Failed to write the tokens", e)
                Result.failure(e)
            }

        override suspend fun get(): StoredTokensResult {
            val preferences =
                try {
                    dataStore.data.first()
                } catch (e: IOException) {
                    logger.e(TAG, "Failed to read the token store", e)
                    return StoredTokensResult.ReadFailed(e)
                }

            // A partial write is treated as no session: two thirds of a token pair is not one.
            val encryptedAccessToken = preferences[ACCESS_TOKEN_KEY] ?: return StoredTokensResult.Empty
            val encryptedRefreshToken = preferences[REFRESH_TOKEN_KEY] ?: return StoredTokensResult.Empty
            val expiresAt = preferences[EXPIRES_AT_KEY] ?: return StoredTokensResult.Empty

            return try {
                StoredTokensResult.Found(
                    StoredTokens(
                        accessToken =
                            cryptoEngine
                                .decrypt(
                                    encryptedAccessToken.decodeBase64(),
                                    ACCESS_TOKEN_AAD,
                                ).decodeUtf8(),
                        refreshToken =
                            cryptoEngine
                                .decrypt(
                                    encryptedRefreshToken.decodeBase64(),
                                    REFRESH_TOKEN_AAD,
                                ).decodeUtf8(),
                        expiresAt = expiresAt,
                    ),
                )
            } catch (e: GeneralSecurityException) {
                logger.e(TAG, "Stored tokens could not be decrypted", e)
                StoredTokensResult.DecryptionFailed(e)
            } catch (e: IllegalArgumentException) {
                // Base64 decoding of a truncated or hand-edited value: same dead end.
                logger.e(TAG, "Stored tokens are not valid Base64", e)
                StoredTokensResult.DecryptionFailed(e)
            } catch (e: IOException) {
                // Raised while reading the keyset the decryption needs, not the tokens.
                logger.e(TAG, "Failed to read the keyset needed to decrypt the tokens", e)
                StoredTokensResult.ReadFailed(e)
            }
        }

        override suspend fun clear(): Result<Unit> =
            try {
                dataStore.edit { preferences ->
                    preferences.remove(ACCESS_TOKEN_KEY)
                    preferences.remove(REFRESH_TOKEN_KEY)
                    preferences.remove(EXPIRES_AT_KEY)
                }
                Result.success(Unit)
            } catch (e: IOException) {
                logger.e(TAG, "Failed to clear the tokens", e)
                Result.failure(e)
            }
    }

private fun ByteArray.encodeBase64(): String = Base64.getEncoder().encodeToString(this)

private fun String.decodeBase64(): ByteArray = Base64.getDecoder().decode(this)

private fun ByteArray.decodeUtf8(): String = toString(Charsets.UTF_8)
