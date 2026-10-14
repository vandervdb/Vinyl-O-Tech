package org.vander.spotifyclient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.core.domain.queue.QueueRepository
import org.vander.spotifyclient.data.repository.SpotifyQueueRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class SpotifyQueueModule {
    @Binds
    @Singleton
    abstract fun bindQueueRepository(impl: SpotifyQueueRepository): QueueRepository
}
