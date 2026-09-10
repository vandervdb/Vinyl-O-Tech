package org.vander.spotifyclient.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.vander.core.domain.auth.ITokenProvider
import org.vander.core.logger.Logger
import org.vander.spotifyclient.network.AuthHeaderPlugin
import org.vander.spotifyclient.network.KtorClientConfig
import javax.inject.Singleton
import io.ktor.client.plugins.logging.Logger as KtorLogger

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideKtorClient(
        tokenProvider: ITokenProvider,
        config: KtorClientConfig,
        outputLogger: Logger,
    ): HttpClient =
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        prettyPrint = true
                        isLenient = true
                    },
                )
            }

            if (config.enableAuthPlugin) {
                install(AuthHeaderPlugin) {
                    this.tokenProvider = tokenProvider
                }
            }

            install(Logging) {
                logger =
                    object : KtorLogger {
                        override fun log(message: String) {
                            outputLogger.d("KtorLogger", message)
                        }
                    }
                outputLogger.d("KtorLogger", "Ktor logger installed")
                level = config.logLevel
            }

            defaultRequest {
                url(config.baseUrl)
            }
        }
}
