package org.vander.spotifyclient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.spotifyclient.data.remote.datasource.RemoteQueueDataSource
import org.vander.spotifyclient.domain.datasource.IRemoteQueueDataSource

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteQueueModule {
    @Binds
    abstract fun bindRemotePlaylistDataSource(impl: RemoteQueueDataSource): IRemoteQueueDataSource
}
