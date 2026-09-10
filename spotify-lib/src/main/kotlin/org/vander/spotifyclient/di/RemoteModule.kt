package org.vander.spotifyclient.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.spotifyclient.data.appremote.SpotifyRemoteConnector
import org.vander.spotifyclient.domain.appremote.RemoteConnector
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteModule {
    @Provides
    @Singleton
    fun provideSpotifyRemoteConnector(): RemoteConnector = SpotifyRemoteConnector()
}
