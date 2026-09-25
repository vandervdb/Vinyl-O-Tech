package org.vander.spotifyclient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.core.domain.recent.RecentlyPlayedRepository
import org.vander.spotifyclient.data.repository.SpotifyRecentlyPlayedRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SpotifyRecentlyPlayedModule {
    @Binds
    @Singleton
    abstract fun bindRecentlyPlayedRepository(impl: SpotifyRecentlyPlayedRepository): RecentlyPlayedRepository
}
