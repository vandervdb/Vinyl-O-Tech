package org.vander.spotifyclient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.spotifyclient.data.local.SpotifyTokenProvider
import org.vander.spotifyclient.data.local.TokenProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class TokenProviderModule {
    @Binds
    @Singleton
    abstract fun bindTokenProvider(impl: SpotifyTokenProvider): TokenProvider
}
