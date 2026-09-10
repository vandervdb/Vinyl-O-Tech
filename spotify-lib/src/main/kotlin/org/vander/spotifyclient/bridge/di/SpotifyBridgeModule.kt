package org.vander.spotifyclient.bridge.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.spotifyclient.bridge.SpotifyBridge
import org.vander.spotifyclient.bridge.SpotifyBridgeApi
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SpotifyBridgeModule {
    @Binds
    @Singleton
    abstract fun bindSpotifyBridge(impl: SpotifyBridge): SpotifyBridgeApi
}
