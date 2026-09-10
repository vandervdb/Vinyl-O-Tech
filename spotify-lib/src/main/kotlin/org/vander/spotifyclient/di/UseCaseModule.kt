package org.vander.spotifyclient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.spotifyclient.domain.usecase.SpotifyRemoteUseCase
import org.vander.spotifyclient.domain.usecase.SpotifyRemoteUseCaseImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {
    @Binds
    @Singleton
    abstract fun bindSpotifyRemoteUseCase(impl: SpotifyRemoteUseCaseImpl): SpotifyRemoteUseCase
}
