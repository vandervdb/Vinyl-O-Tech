package org.vander.spotifyclient.data.local

import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.vander.core.logger.Logger
import org.vander.spotifyclient.domain.auth.AuthRepository
import javax.inject.Inject

internal class SpotifyTokenProvider
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        private val logger: Logger,
    ) : TokenProvider {
        private val refreshMutex = Mutex()

        override suspend fun currentToken(): BearerTokens? {
            val tokens = authRepository.getTokens().getOrNull()
            return tokens?.let { BearerTokens(accessToken = it.accessToken, refreshToken = it.refreshToken) }
        }

        /**
         * One refresh at a time, and none for a token that was already replaced.
         *
         * Ktor already coalesces the concurrent 401s of one client, and it is the only caller
         * today. The lock is kept so the invariant holds by construction rather than by that
         * fact: a second caller — a proactive refresh, another client — would otherwise race
         * this one on a rotating refresh token.
         */
        override suspend fun refresh(staleAccessToken: String?): Result<Unit> =
            refreshMutex.withLock {
                val stored =
                    authRepository.getTokens().getOrElse { return Result.failure(it) }
                        ?: run {
                            logger.e(TAG, "No stored session to refresh")
                            return Result.failure(IllegalStateException("No session to refresh"))
                        }
                if (stored.accessToken != staleAccessToken) return Result.success(Unit)

                val renewed =
                    authRepository
                        .fetchRefreshedTokenResponse(stored.refreshToken)
                        .getOrElse { return Result.failure(it) }
                // Once Spotify has answered, the old refresh token may already be revoked: losing
                // this write to a cancellation would leave the session unrecoverable.
                withContext(NonCancellable) { authRepository.storeTokenResponse(renewed) }
            }

        private companion object {
            const val TAG = "SpotifyTokenProvider"
        }
    }
