package org.vander.spotifyclient.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.LogLevel
import org.vander.core.domain.auth.ITokenProvider
import org.vander.core.logger.Logger
import org.vander.spotifyclient.data.remote.datasource.RemoteAuthDataSource
import org.vander.spotifyclient.data.remote.datasource.SpotifyRemoteAuthDataSource
import org.vander.spotifyclient.network.KtorClientConfig
import org.vander.spotifyclient.utils.HTTPS_ACCOUNTS_SPOTIFY_COM_API
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RemoteAuthModule {
    @Binds
    abstract fun bindRemoteAuthDataSource(impl: SpotifyRemoteAuthDataSource): RemoteAuthDataSource

    companion object {
        @Provides
        @Singleton
        @Named("AuthHttpClientConfig")
        fun provideAuthHttpClientConfig(): KtorClientConfig =
            KtorClientConfig(
                baseUrl = HTTPS_ACCOUNTS_SPOTIFY_COM_API,
                enableAuthPlugin = false,
                logLevel = LogLevel.ALL,
            )

        @Provides
        @Singleton
        @Named("AuthHttpClient")
        fun provideAuthHttpClient(
            tokenProvider: ITokenProvider,
            logger: Logger,
            @Named("AuthHttpClientConfig") config: KtorClientConfig,
        ): HttpClient = NetworkModule.provideKtorClient(tokenProvider, config, logger)
    }
}
