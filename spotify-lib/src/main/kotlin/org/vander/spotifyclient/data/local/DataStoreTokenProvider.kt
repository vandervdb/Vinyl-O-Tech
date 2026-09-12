package org.vander.spotifyclient.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.vander.core.domain.auth.ITokenProvider
import org.vander.spotifyclient.domain.auth.IDataStoreManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Read-only [ITokenProvider] adapter over [IDataStoreManager].
 *
 * Adapter pattern: it exposes the same data through a narrower contract, so the network
 * layer can read the token without gaining the ability to clear the session.
 */
@Singleton
class DataStoreTokenProvider
    @Inject
    constructor(
        val dataStoreManager: IDataStoreManager,
    ) : ITokenProvider {
        override val tokenFlow: Flow<String?>
            get() = dataStoreManager.accessTokenFlow

        override suspend fun getAccessToken(): String? = dataStoreManager.accessTokenFlow.firstOrNull()
    }
