package org.vander.spotifyclient.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vander.spotifyclient.BuildConfig
import org.vander.spotifyclient.domain.auth.SpotifyAuthConfig
import org.vander.spotifyclient.utils.REDIRECT_URI

@Module
@InstallIn(SingletonComponent::class)
internal object AuthConfigModule {
    @Provides
    fun provideAuthConfig(): SpotifyAuthConfig {
        val clientId = BuildConfig.CLIENT_ID
        val redirectUri = REDIRECT_URI
        return SpotifyAuthConfig(clientId, redirectUri)
    }
}
