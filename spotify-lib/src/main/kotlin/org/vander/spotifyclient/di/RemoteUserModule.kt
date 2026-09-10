package org.vander.spotifyclient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.spotifyclient.data.remote.datasource.RemoteUserDataSource
import org.vander.spotifyclient.data.repository.SpotifyUserRepository
import org.vander.spotifyclient.domain.datasource.IRemoteUserDataSource
import org.vander.spotifyclient.domain.repository.UserRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteUserModule {
    @Binds
    @Singleton
    abstract fun bindRemoteUserDataSource(impl: RemoteUserDataSource): IRemoteUserDataSource

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: SpotifyUserRepository): UserRepository
}
