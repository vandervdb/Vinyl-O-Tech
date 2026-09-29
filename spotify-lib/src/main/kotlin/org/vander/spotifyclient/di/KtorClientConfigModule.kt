package org.vander.spotifyclient.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.LogLevel
import org.vander.core.logger.Logger
import org.vander.spotifyclient.data.local.TokenProvider
import org.vander.spotifyclient.network.KtorClientConfig
import org.vander.spotifyclient.network.createKtorClient
import org.vander.spotifyclient.utils.HTTPS_API_SPOTIFY_COM_V_1
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object KtorClientConfigModule {
    @Provides
    @Singleton
    @Named("public_api_v1")
    fun providePublicApiV1Config(): KtorClientConfig =
        KtorClientConfig(
            baseUrl = HTTPS_API_SPOTIFY_COM_V_1,
            enableAuthPlugin = false,
            logLevel = LogLevel.INFO,
        )

    @Provides
    @Singleton
    @Named("auth_api_v1")
    fun provideAuthApiV1Config(): KtorClientConfig =
        KtorClientConfig(
            baseUrl = HTTPS_API_SPOTIFY_COM_V_1,
            enableAuthPlugin = true,
            logLevel = LogLevel.INFO,
        )

    @Provides
    @Singleton
    @Named("public_api_v1_client")
    fun providePublicApiV1Client(
        logger: Logger,
        @Named("public_api_v1") config: KtorClientConfig,
    ): HttpClient = createKtorClient(config, logger)

    @Provides
    @Singleton
    @Named("auth_api_v1_client")
    fun provideAuthApiV1Client(
        tokenProvider: TokenProvider,
        logger: Logger,
        @Named("auth_api_v1") config: KtorClientConfig,
    ): HttpClient = createKtorClient(config, logger, tokenProvider)
}
