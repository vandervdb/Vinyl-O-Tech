package org.vander.spotifyclient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.spotifyclient.data.remote.datasource.RemoteRecentlyPlayedDataSource
import org.vander.spotifyclient.data.remote.datasource.SpotifyRemoteRecentlyPlayedDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RemoteRecentlyPlayedModule {
    @Binds
    @Singleton
    abstract fun bindRemoteRecentlyPlayedDataSource(
        impl: SpotifyRemoteRecentlyPlayedDataSource,
    ): RemoteRecentlyPlayedDataSource
}
