package org.vander.spotifyclient.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.core.domain.auth.IAuthRepository
import org.vander.core.logger.Logger
import org.vander.spotifyclient.data.remote.datasource.AuthRemoteDataSource
import org.vander.spotifyclient.data.repository.AuthRepository
import org.vander.spotifyclient.domain.auth.IDataStoreManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthProvidesModule {
    @Provides
    @Singleton
    fun provideAuthRepository(
        authRemoteDataSource: AuthRemoteDataSource,
        dataStoreManager: IDataStoreManager,
        logger: Logger,
    ): IAuthRepository = AuthRepository(authRemoteDataSource, dataStoreManager, logger)
}
