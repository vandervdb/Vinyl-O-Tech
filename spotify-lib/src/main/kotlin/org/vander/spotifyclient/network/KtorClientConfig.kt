package org.vander.spotifyclient.network

import io.ktor.client.plugins.logging.LogLevel

data class KtorClientConfig(
    val baseUrl: String,
    val enableAuthPlugin: Boolean = true,
    val logLevel: LogLevel = LogLevel.NONE,
)
