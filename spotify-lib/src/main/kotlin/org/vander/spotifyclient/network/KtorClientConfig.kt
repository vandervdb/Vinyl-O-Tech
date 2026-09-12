package org.vander.spotifyclient.network

import io.ktor.client.plugins.logging.LogLevel

/**
 * Describes one of the app's Ktor clients, so the several of them differ by data rather than
 * by duplicated builder code.
 *
 * @property enableAuthPlugin `false` for the accounts client, which authenticates with a
 *   Basic header instead of a bearer token.
 */
data class KtorClientConfig(
    val baseUrl: String,
    val enableAuthPlugin: Boolean = true,
    val logLevel: LogLevel = LogLevel.NONE,
)
