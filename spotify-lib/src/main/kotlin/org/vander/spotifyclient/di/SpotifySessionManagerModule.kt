package org.vander.spotifyclient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.spotifyclient.data.session.SpotifySessionManager
import org.vander.spotifyclient.domain.session.SessionManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class SpotifySessionManagerModule {
    @Binds
    @Singleton
    abstract fun bindSessionManager(spotifySessionManager: SpotifySessionManager): SessionManager
}
