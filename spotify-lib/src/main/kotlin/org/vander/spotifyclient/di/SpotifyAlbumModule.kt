package org.vander.spotifyclient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.spotifyclient.data.repository.SpotifyAlbumRepository
import org.vander.spotifyclient.domain.album.AlbumRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class SpotifyAlbumModule {
    @Binds
    @Singleton
    abstract fun bindSpotifyAlbumRepository(impl: SpotifyAlbumRepository): AlbumRepository
}
