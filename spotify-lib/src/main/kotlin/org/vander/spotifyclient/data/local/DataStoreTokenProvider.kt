package org.vander.spotifyclient.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.vander.core.domain.auth.ITokenProvider
import org.vander.spotifyclient.domain.auth.IDataStoreManager
import javax.inject.Inject
import javax.inject.Singleton

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
