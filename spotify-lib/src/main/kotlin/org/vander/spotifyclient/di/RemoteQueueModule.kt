package org.vander.spotifyclient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.spotifyclient.data.remote.datasource.RemoteQueueDataSource
import org.vander.spotifyclient.data.remote.datasource.SpotifyRemoteQueueDataSource

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RemoteQueueModule {
    @Binds
    abstract fun bindRemotePlaylistDataSource(impl: SpotifyRemoteQueueDataSource): RemoteQueueDataSource
}
