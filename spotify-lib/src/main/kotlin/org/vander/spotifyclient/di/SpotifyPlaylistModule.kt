package org.vander.spotifyclient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.core.domain.playlist.PlaylistRepository
import org.vander.spotifyclient.data.repository.SpotifyPlaylistRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class SpotifyPlaylistModule {
    @Binds
    @Singleton
    abstract fun bindSpotifyPlaylistRepository(impl: SpotifyPlaylistRepository): PlaylistRepository
}
