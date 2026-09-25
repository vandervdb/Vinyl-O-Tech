package org.vander.spotifyclient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.core.domain.player.PlayerController
import org.vander.spotifyclient.data.player.SpotifyPlayerController
import javax.inject.Singleton

/**
 * Binds [PlayerController] to [PlayerUseCase], as a singleton.
 *
 * The scope is the point of this module. Unscoped, each ViewModel received its own instance,
 * and only the one whose collectors had been started published anything.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PlayerControllerModule {
    @Binds
    @Singleton
    abstract fun bindPlayerController(impl: SpotifyPlayerController): PlayerController
}
