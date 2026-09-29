package org.vander.spotifyclient.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.vander.core.logger.Logger
import org.vander.spotifyclient.data.local.TokenProvider
import org.vander.spotifyclient.utils.HTTPS_API_SPOTIFY_COM_V_1
import io.ktor.client.plugins.logging.Logger as KtorLogger

/**
 * Builds one of the app's Ktor clients from its [KtorClientConfig].
 *
 * A plain factory, not a Hilt binding: each `@Provides` of a `@Named` client calls it, and the
 * scope of that `@Provides` is what makes the client unique — this function creates a new
 * [HttpClient] on every call.
 *
 * @param tokenProvider required when [KtorClientConfig.enableAuthPlugin] is set; `null` for the
 *   accounts client, which must not depend on it (see `RemoteAuthModule`).
 */
internal fun createKtorClient(
    config: KtorClientConfig,
    outputLogger: Logger,
    tokenProvider: TokenProvider? = null,
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
            checkNotNull(tokenProvider) { "A client with the auth plugin needs a TokenProvider" }
            install(Auth) {
                bearer {
                    loadTokens {
                        tokenProvider.currentToken()
                    }
                    refreshTokens {
                        tokenProvider
                            .refresh(staleAccessToken = oldTokens?.accessToken)
                            .map { tokenProvider.currentToken() }
                            .getOrNull()
                    }
                    sendWithoutRequest { request ->
                        request.url.host == Url(HTTPS_API_SPOTIFY_COM_V_1).host
                    }
                }
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
