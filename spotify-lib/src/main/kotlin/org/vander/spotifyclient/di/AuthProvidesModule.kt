package org.vander.spotifyclient.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.core.domain.auth.IAuthRepository
import org.vander.core.logger.Logger
import org.vander.core.security.api.SecureTokenStorage
import org.vander.spotifyclient.data.remote.datasource.RemoteAuthDataSource
import org.vander.spotifyclient.data.repository.AuthRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object AuthProvidesModule {
    @Provides
    @Singleton
    fun provideAuthRepository(
        remoteAuthDataSource: RemoteAuthDataSource,
        secureTokenStorage: SecureTokenStorage,
        logger: Logger,
    ): IAuthRepository = AuthRepository(remoteAuthDataSource, secureTokenStorage, logger)
}
