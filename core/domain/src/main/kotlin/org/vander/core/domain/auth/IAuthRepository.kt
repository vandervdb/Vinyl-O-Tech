package org.vander.core.domain.auth

import org.vander.core.domain.data.TokenResponse

/**
 * Obtains and persists the Spotify Web API tokens.
 *
 * Fetching and storing are two operations under two names: one hits the accounts service, the
 * other writes to disk, and they fail for unrelated reasons. Every operation returns a
 * [Result] rather than throwing — storage failures are recoverable and belong to the caller.
 *
 * Implementations live in the `spotify-lib` data layer; consumers depend on this interface only.
 */
interface IAuthRepository {
    /**
     * Exchanges an authorization code against the accounts service. Stores nothing.
     *
     * @param authorizationCode the **code** returned by the Spotify login flow, not a token.
     */
    suspend fun fetchTokenResponse(authorizationCode: String): Result<TokenResponse>

    /**
     * Persists a token pair.
     *
     * A null [TokenResponse.refreshToken] keeps the currently stored one; the call fails only
     * when the response carries none and nothing was stored either.
     */
    suspend fun storeTokenResponse(tokenResponse: TokenResponse): Result<Unit>

    /**
     * @return a success holding an empty string when no session is stored — absence is not a
     *   failure. A failure means the store could not be read or its content could not be
     *   decrypted, and the error is logged either way.
     */
    suspend fun getAccessToken(): Result<String>

    suspend fun clearAccessToken(): Result<Unit>
}
