package org.vander.spotifyclient.domain.auth

import org.vander.core.dto.TokenResponseDto

/**
 * Exchanges an authorization code for a token pair against the Spotify accounts service.
 *
 * The only call in the app that authenticates with the client secret rather than a bearer
 * token, which is why it sits apart from the other data sources.
 */
interface IAuthRemoteDatasource {
    /**
     * @param code the authorization code from the login flow, usable once.
     * @return failure carrying the API's message on a non-200, including an expired or replayed
     *   code.
     */
    suspend fun fetchAccessToken(code: String): Result<TokenResponseDto>
}
