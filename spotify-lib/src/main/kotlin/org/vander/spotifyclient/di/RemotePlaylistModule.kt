package org.vander.spotifyclient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.spotifyclient.data.remote.datasource.RemotePlaylistDataSource
import org.vander.spotifyclient.domain.datasource.IRemotePlaylistDataSource

@Module
@InstallIn(SingletonComponent::class)
abstract class RemotePlaylistModule {
    @Binds
    abstract fun bindRemotePlaylistDataSource(impl: RemotePlaylistDataSource): IRemotePlaylistDataSource
}
