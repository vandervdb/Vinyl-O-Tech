package org.vander.spotifyclient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.spotifyclient.data.remote.datasource.RemoteLibraryDataSource
import org.vander.spotifyclient.data.remote.datasource.SpotifyRemoteLibraryDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class SpotifyLibraryRemoteModule {
    @Binds
    @Singleton
    abstract fun bindLibraryRemoteDataSource(impl: SpotifyRemoteLibraryDataSource): RemoteLibraryDataSource
}
