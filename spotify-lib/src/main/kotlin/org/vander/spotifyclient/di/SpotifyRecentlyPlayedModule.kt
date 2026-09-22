package org.vander.spotifyclient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.spotifyclient.data.repository.SpotifyRecentlyPlayedRepositoryImpl
import org.vander.spotifyclient.domain.repository.RecentlyPlayedRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SpotifyRecentlyPlayedModule {
    @Binds
    @Singleton
    abstract fun bindRecentlyPlayedRepository(impl: SpotifyRecentlyPlayedRepositoryImpl): RecentlyPlayedRepository
}
