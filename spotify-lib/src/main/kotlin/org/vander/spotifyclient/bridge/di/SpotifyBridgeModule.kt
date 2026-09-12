package org.vander.spotifyclient.bridge.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.spotifyclient.bridge.SpotifyBridge
import org.vander.spotifyclient.bridge.SpotifyBridgeApi
import javax.inject.Singleton

/**
 * Binds [SpotifyBridgeApi] to [SpotifyBridge] as a singleton.
 *
 * `@Binds` on an `abstract class` rather than `@Provides`: Hilt can build [SpotifyBridge]
 * itself from its `@Inject` constructor, so only the interface-to-implementation link is
 * missing and `@Binds` generates no factory body for it.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SpotifyBridgeModule {
    @Binds
    @Singleton
    abstract fun bindSpotifyBridge(impl: SpotifyBridge): SpotifyBridgeApi
}
