package org.vander.spotifyclient.data.local

import io.ktor.client.plugins.auth.providers.BearerTokens

/**
 * Read-only view over the stored access token, for components that consume a token
 * but must never write one — typically the Ktor auth plugin and the App Remote connector.
 *
 * Narrower than [org.vander.core.domain.session.AuthRepository] on purpose: interface segregation,
 * so a consumer that only reads cannot clear the session by mistake.
 */
internal interface TokenProvider {
    /**
     * @return the current token, or `null` when none is stored. Reads the first value of
     *   [tokenFlow], so it suspends until storage has been read once.
     */
    suspend fun currentToken(): BearerTokens?

    suspend fun refresh(staleAccessToken: String?): Result<Unit>
}
