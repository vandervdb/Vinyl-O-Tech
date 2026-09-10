package org.vander.spotifyclient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.core.domain.player.PlayerStateRepository
import org.vander.spotifyclient.data.repository.DefaultPlayerStateRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindPlayerStateRepository(impl: DefaultPlayerStateRepository): PlayerStateRepository
}
