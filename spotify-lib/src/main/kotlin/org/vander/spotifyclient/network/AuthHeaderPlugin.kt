package org.vander.spotifyclient.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.util.AttributeKey
import org.vander.core.domain.auth.ITokenProvider
import javax.inject.Inject

/**
 * Ktor plugin adding the `Authorization: Bearer` header to every request of the client it is
 * installed on.
 *
 * The token is read from [ITokenProvider] inside the interceptor, once per request, so a
 * refresh is picked up without rebuilding the client. A blank token means the header is
 * simply omitted and the request still goes out — the plugin never short-circuits a call.
 */
class AuthHeaderPlugin
    @Inject
    constructor(
        val tokenProvider: ITokenProvider,
    ) {
        companion object : HttpClientPlugin<Config, AuthHeaderPlugin> {
            override val key: AttributeKey<AuthHeaderPlugin> = AttributeKey("AuthHeaderPlugin")

            override fun prepare(block: Config.() -> Unit): AuthHeaderPlugin {
                val config = Config().apply(block)
                requireNotNull(config.tokenProvider) {
                    "AuthHeaderPlugin requires a non-null ITokenProvider"
                }
                return AuthHeaderPlugin(config.tokenProvider!!)
            }

            override fun install(
                plugin: AuthHeaderPlugin,
                scope: HttpClient,
            ) {
                scope.requestPipeline.intercept(HttpRequestPipeline.State) {
                    val token = plugin.tokenProvider.getAccessToken()
                    if (!token.isNullOrBlank()) {
                        context.headers.append("Authorization", "Bearer $token")
                    }
                    proceed()
                }
            }
        }

        class Config {
            var tokenProvider: ITokenProvider? = null
        }
    }
