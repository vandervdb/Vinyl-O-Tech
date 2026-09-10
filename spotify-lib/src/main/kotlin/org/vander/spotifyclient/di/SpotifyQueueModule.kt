package org.vander.spotifyclient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.spotifyclient.data.repository.SpotifyQueueRepositoryImpl
import org.vander.spotifyclient.domain.repository.SpotifyQueueRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class SpotifyQueueModule {
    @Binds
    abstract fun bindQueueRepository(impl: SpotifyQueueRepositoryImpl): SpotifyQueueRepository
}
