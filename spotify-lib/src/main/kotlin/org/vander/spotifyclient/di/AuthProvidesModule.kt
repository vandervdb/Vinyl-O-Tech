package org.vander.spotifyclient.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.core.logger.Logger
import org.vander.core.security.api.SecureTokenStorage
import org.vander.spotifyclient.data.remote.datasource.RemoteAuthDataSource
import org.vander.spotifyclient.data.repository.SpotifyAuthRepository
import org.vander.spotifyclient.domain.auth.AuthRepository
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
    ): AuthRepository = SpotifyAuthRepository(remoteAuthDataSource, secureTokenStorage, logger)
}
