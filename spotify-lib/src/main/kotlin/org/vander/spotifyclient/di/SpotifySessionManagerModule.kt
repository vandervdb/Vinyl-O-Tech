package org.vander.spotifyclient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.spotifyclient.data.session.SpotifySessionManagerImpl
import org.vander.spotifyclient.domain.data.session.SpotifySessionManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SpotifySessionManagerModule {
    @Binds
    @Singleton
    abstract fun bindSpotifySessionManager(spotifySessionManagerImpl: SpotifySessionManagerImpl): SpotifySessionManager
}
