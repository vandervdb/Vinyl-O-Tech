package org.vander.spotifyclient.data.repository

import org.vander.core.domain.auth.IAuthRepository
import org.vander.core.domain.data.TokenResponse
import org.vander.core.logger.Logger
import org.vander.core.security.api.SecureTokenStorage
import org.vander.core.security.api.StoredTokensResult
import org.vander.spotifyclient.data.remote.datasource.RemoteAuthDataSource
import org.vander.spotifyclient.data.remote.mapper.toDomain
import javax.inject.Inject

/**
 * Exchanges an authorization code against the accounts service, and keeps the resulting
 * tokens in [SecureTokenStorage] — encrypted, unlike the plain `DataStoreManager` this
 * repository used to write to.
 *
 * Reads and writes now go through the same store: a session written here is the one read back.
 */
internal class AuthRepository
    @Inject
    constructor(
        private val remoteAuthDataSource: RemoteAuthDataSource,
        private val secureTokenStorage: SecureTokenStorage,
        private val logger: Logger,
    ) : IAuthRepository {
        companion object Companion {
            private const val TAG = "AuthRepository"
        }

        override suspend fun fetchTokenResponse(authorizationCode: String): Result<TokenResponse> =
            remoteAuthDataSource
                .fetchAccessToken(authorizationCode)
                .map { dto -> dto.toDomain() }
                .onFailure { logger.e(TAG, "Error fetching the token response", it) }

        override suspend fun storeTokenResponse(tokenResponse: TokenResponse): Result<Unit> {
            val refreshToken = tokenResponse.refreshToken ?: storedRefreshToken()
            if (refreshToken == null) {
                logger.e(TAG, "No refresh token in the response and none stored")
                return Result.failure(IllegalStateException("No refresh token available"))
            }

            return secureTokenStorage
                .save(tokenResponse.accessToken, refreshToken, tokenResponse.expiresAt)
                .onFailure { logger.e(TAG, "Error saving the tokens", it) }
        }

        override suspend fun getAccessToken(): Result<String> =
            when (val result = secureTokenStorage.get()) {
                is StoredTokensResult.Found -> Result.success(result.tokens.accessToken)
                StoredTokensResult.Empty -> Result.success("")
                is StoredTokensResult.ReadFailed -> {
                    logger.e(TAG, "Could not read the stored session", result.cause)
                    Result.failure(result.cause)
                }

                is StoredTokensResult.DecryptionFailed -> {
                    logger.e(TAG, "Stored session is unreadable, clearing it", result.cause)
                    secureTokenStorage.clear()
                    Result.failure(result.cause)
                }
            }

        override suspend fun clearAccessToken(): Result<Unit> = secureTokenStorage.clear()

        private suspend fun storedRefreshToken(): String? =
            (secureTokenStorage.get() as? StoredTokensResult.Found)?.tokens?.refreshToken
    }
