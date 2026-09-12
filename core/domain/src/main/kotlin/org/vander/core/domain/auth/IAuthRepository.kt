package org.vander.core.domain.auth

/**
 * Persists the Spotify Web API access token obtained by the authorization flow.
 *
 * Every operation returns a [Result] rather than throwing: storage is backed by DataStore,
 * whose failures are recoverable and must be surfaced to the caller, not crash it.
 * Implementations live in the `spotify-lib` data layer; consumers depend on this interface only.
 */
interface IAuthRepository {
    /**
     * Obtains an access token and stores it.
     *
     * @param token the authorization **code** returned by the Spotify login flow, not a token:
     *   the implementation exchanges it against the accounts service and stores what comes back.
     * @return failure if the exchange or the write failed; the error is logged either way.
     */
    suspend fun storeAccessToken(token: String): Result<Unit>

    /**
     * @return a success holding an empty string when no token was ever stored — absence and
     *   failure are not distinguished here, only an I/O error produces a failure.
     */
    suspend fun getAccessToken(): Result<String>

    suspend fun clearAccessToken(): Result<Unit>
}
