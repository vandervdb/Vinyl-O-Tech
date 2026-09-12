package org.vander.core.domain.auth

import kotlinx.coroutines.flow.Flow

/**
 * Read-only view over the stored access token, for components that consume a token
 * but must never write one — typically the Ktor auth plugin and the App Remote connector.
 *
 * Narrower than [org.vander.core.domain.auth.IAuthRepository] on purpose: interface segregation,
 * so a consumer that only reads cannot clear the session by mistake.
 */
interface ITokenProvider {
    /** Emits the current token and every later change; `null` means no session. */
    val tokenFlow: Flow<String?>

    /**
     * @return the current token, or `null` when none is stored. Reads the first value of
     *   [tokenFlow], so it suspends until storage has been read once.
     */
    suspend fun getAccessToken(): String?
}
