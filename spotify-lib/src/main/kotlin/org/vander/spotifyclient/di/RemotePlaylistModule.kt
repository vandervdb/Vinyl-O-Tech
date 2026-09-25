package org.vander.spotifyclient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.spotifyclient.data.remote.datasource.RemotePlaylistDataSource
import org.vander.spotifyclient.data.remote.datasource.SpotifyRemotePlaylistDataSource

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RemotePlaylistModule {
    @Binds
    abstract fun bindRemotePlaylistDataSource(impl: SpotifyRemotePlaylistDataSource): RemotePlaylistDataSource
}
