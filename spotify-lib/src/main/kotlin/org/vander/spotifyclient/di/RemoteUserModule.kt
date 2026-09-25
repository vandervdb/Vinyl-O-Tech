package org.vander.spotifyclient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.core.domain.user.UserRepository
import org.vander.spotifyclient.data.remote.datasource.RemoteUserDataSource
import org.vander.spotifyclient.data.remote.datasource.SpotifyRemoteUserDataSource
import org.vander.spotifyclient.data.repository.SpotifyUserRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RemoteUserModule {
    @Binds
    @Singleton
    abstract fun bindRemoteUserDataSource(impl: SpotifyRemoteUserDataSource): RemoteUserDataSource

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: SpotifyUserRepository): UserRepository
}
