package org.vander.spotifyclient.data.repository

import org.vander.core.domain.data.TokenResponse
import org.vander.core.logger.Logger
import org.vander.core.security.api.SecureTokenStorage
import org.vander.core.security.api.StoredTokensResult
import org.vander.spotifyclient.data.remote.datasource.RemoteAuthDataSource
import org.vander.spotifyclient.data.remote.mapper.toDomain
import org.vander.spotifyclient.domain.auth.AuthRepository
import org.vander.spotifyclient.domain.auth.SessionTokens
import javax.inject.Inject

/**
 * Exchanges an authorization code against the accounts service, and keeps the resulting
 * tokens in [SecureTokenStorage] — encrypted, unlike the plain `DataStoreManager` this
 * repository used to write to.
 *
 * Reads and writes now go through the same store: a session written here is the one read back.
 */
internal class SpotifyAuthRepository
    @Inject
    constructor(
        private val remoteAuthDataSource: RemoteAuthDataSource,
        private val secureTokenStorage: SecureTokenStorage,
        private val logger: Logger,
    ) : AuthRepository {
        companion object Companion {
            private const val TAG = "SpotifyAuthRepository"
        }

        override suspend fun fetchTokenResponse(authorizationCode: String): Result<TokenResponse> =
            remoteAuthDataSource
                .fetchAccessToken(authorizationCode)
                .map { dto -> dto.toDomain() }
                .onFailure { logger.e(TAG, "Error fetching the token response", it) }

        override suspend fun fetchRefreshedTokenResponse(refreshToken: String): Result<TokenResponse> =
            remoteAuthDataSource
                .refreshAccessToken(refreshToken)
                .map { dto -> dto.toDomain() }
                .onFailure { logger.e(TAG, "Error fetching the refreshed token response", it) }

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

        override suspend fun getTokens(): Result<SessionTokens?> =
            when (val result = secureTokenStorage.get()) {
                is StoredTokensResult.Found ->
                    Result.success(
                        SessionTokens(result.tokens.accessToken, result.tokens.refreshToken, result.tokens.expiresAt),
                    )
                StoredTokensResult.Empty -> Result.success(null)
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

        override suspend fun clearSessionTokens(): Result<Unit> = secureTokenStorage.clear()

        private suspend fun storedRefreshToken(): String? =
            (secureTokenStorage.get() as? StoredTokensResult.Found)?.tokens?.refreshToken
    }
