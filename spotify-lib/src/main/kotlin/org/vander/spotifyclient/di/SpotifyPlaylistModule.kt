package org.vander.spotifyclient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.spotifyclient.data.repository.SpotifyPlaylistRepositoryImpl
import org.vander.spotifyclient.domain.repository.SpotifyPlaylistRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class SpotifyPlaylistModule {
    @Binds
    @Singleton
    abstract fun bindSpotifyPlaylistRepository(impl: SpotifyPlaylistRepositoryImpl): SpotifyPlaylistRepository
}
