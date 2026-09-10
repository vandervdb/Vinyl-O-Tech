package org.vander.spotifyclient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.spotifyclient.data.auth.SpotifyAuthClient
import org.vander.spotifyclient.domain.auth.ISpotifyAuthClient

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds
    abstract fun bindSpotifyAuthClient(impl: SpotifyAuthClient): ISpotifyAuthClient
}
