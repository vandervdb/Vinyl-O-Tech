package org.vander.spotifyclient.data.local

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.vander.core.domain.data.TokenResponse
import org.vander.core.logger.test.FakeLogger
import org.vander.spotifyclient.domain.auth.AuthRepository
import org.vander.spotifyclient.domain.auth.SessionTokens

class SpotifyTokenProviderTest {
    @Test
    fun `two concurrent refreshes of the same stale token hit the network once`() =
        runTest {
            // The fake suspends during the "network" call, so without the lock both callers
            // would read token A and both would refresh.
            val repository = FakeAuthRepository(stored = session("A"))
            val provider = SpotifyTokenProvider(repository, FakeLogger())

            val results =
                listOf(
                    async { provider.refresh(staleAccessToken = "A") },
                    async { provider.refresh(staleAccessToken = "A") },
                ).awaitAll()

            assertTrue(results.all { it.isSuccess })
            assertEquals(1, repository.refreshCalls)
            assertEquals("B", repository.stored?.accessToken)
        }

    @Test
    fun `a token already replaced is not refreshed again`() =
        runTest {
            // The late 401: the request left with A, someone else has stored B since.
            val repository = FakeAuthRepository(stored = session("B"))
            val provider = SpotifyTokenProvider(repository, FakeLogger())

            val result = provider.refresh(staleAccessToken = "A")

            assertTrue(result.isSuccess)
            assertEquals(0, repository.refreshCalls)
        }

    @Test
    fun `a refused refresh token fails and stores nothing`() =
        runTest {
            val repository =
                FakeAuthRepository(
                    stored = session("A"),
                    refreshResult = Result.failure(IllegalStateException("invalid_grant")),
                )
            val provider = SpotifyTokenProvider(repository, FakeLogger())

            val result = provider.refresh(staleAccessToken = "A")

            assertTrue(result.isFailure)
            assertEquals(0, repository.storeCalls)
            assertEquals("A", repository.stored?.accessToken)
        }

    @Test
    fun `no stored session fails and is logged`() =
        runTest {
            val logger = FakeLogger()
            val provider = SpotifyTokenProvider(FakeAuthRepository(stored = null), logger)

            val result = provider.refresh(staleAccessToken = "A")

            assertTrue(result.isFailure)
            assertTrue(logger.contains(FakeLogger.Entry.Level.ERROR, TAG, "No stored session to refresh"))
        }

    private fun session(accessToken: String) = SessionTokens(accessToken, REFRESH_TOKEN, expiresAt = 0L)

    /**
     * In-memory [AuthRepository]: a refresh returns token B after a simulated network delay,
     * and a store keeps the stored refresh token when the response carries none — like the real one.
     */
    private class FakeAuthRepository(
        var stored: SessionTokens?,
        private val refreshResult: Result<TokenResponse> =
            Result.success(TokenResponse(accessToken = "B", expiresAt = 3_600_000L, refreshToken = null)),
    ) : AuthRepository {
        var refreshCalls = 0
            private set
        var storeCalls = 0
            private set

        override suspend fun fetchRefreshedTokenResponse(refreshToken: String): Result<TokenResponse> {
            refreshCalls++
            delay(NETWORK_DELAY_MS)
            return refreshResult
        }

        override suspend fun storeTokenResponse(tokenResponse: TokenResponse): Result<Unit> {
            storeCalls++
            val refreshToken = tokenResponse.refreshToken ?: stored?.refreshToken ?: REFRESH_TOKEN
            stored = SessionTokens(tokenResponse.accessToken, refreshToken, tokenResponse.expiresAt)
            return Result.success(Unit)
        }

        override suspend fun getTokens(): Result<SessionTokens?> = Result.success(stored)

        override suspend fun fetchTokenResponse(authorizationCode: String): Result<TokenResponse> =
            error("Not used by the token provider")

        override suspend fun clearSessionTokens(): Result<Unit> = error("Not used by the token provider")
    }

    private companion object {
        const val TAG = "SpotifyTokenProvider"
        const val REFRESH_TOKEN = "refresh-token"
        const val NETWORK_DELAY_MS = 100L
    }
}
