package org.vander.spotifyclient.data.repository

import org.vander.core.domain.auth.IAuthRepository
import org.vander.core.logger.Logger
import org.vander.spotifyclient.data.remote.datasource.AuthRemoteDataSource
import org.vander.spotifyclient.domain.auth.IDataStoreManager
import javax.inject.Inject

/**
 * Turns an authorization code into a stored access token.
 *
 * Despite its name, [storeAccessToken] takes an authorization *code*, calls the token
 * endpoint with it, and stores the access token it gets back — it does not store what it is
 * handed. Reads go straight to DataStore without hitting the network.
 */
class AuthRepository
    @Inject
    constructor(
        private val authRemoteDataSource: AuthRemoteDataSource,
        private val dataStoreManager: IDataStoreManager,
        private val logger: Logger,
    ) : IAuthRepository {
        companion object Companion {
            private const val TAG = "AuthRepository"
        }

        override suspend fun storeAccessToken(token: String): Result<Unit> =
            authRemoteDataSource
                .fetchAccessToken(token)
                .onFailure { logger.e(TAG, "Error fetching access token", it) }
                .mapCatching { dto ->
                    logger.d(TAG, "Saving access token: ${dto.accessToken}")
                    dataStoreManager.saveAccessToken(dto.accessToken)
                }

        override suspend fun getAccessToken(): Result<String> = dataStoreManager.getAccessToken()

        override suspend fun clearAccessToken(): Result<Unit> = dataStoreManager.clearAccessToken()
    }
