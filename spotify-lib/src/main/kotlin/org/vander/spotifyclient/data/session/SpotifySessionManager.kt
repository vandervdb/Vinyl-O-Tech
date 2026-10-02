package org.vander.spotifyclient.data.session

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.vander.core.domain.error.SessionError
import org.vander.core.domain.session.SessionManager
import org.vander.core.domain.state.SessionState
import org.vander.core.logger.Logger
import org.vander.spotifyclient.domain.appremote.AppRemoteProvider
import org.vander.spotifyclient.domain.auth.AuthRepository
import javax.inject.Inject

/**
 * Runs the session as a state machine over [SessionState]: `Idle` -> `Authorizing` ->
 * `ConnectingRemote` -> `Ready`, with `Failed` reachable from any step.
 *
 * The flow is split across three calls because the authorization leaves the app: the UI
 * registers its launcher through [requestAuthorization], fires it with
 * [launchAuthorizationFlow], and feeds the Activity result back through [handleAuthResult],
 * which then exchanges the code for a token and connects the App Remote.
 *
 * The caller supplies the [CoroutineScope]: this manager is `@Singleton`-scoped and outlives
 * any screen, so it must not own the scope the work runs in.
 */
internal class SpotifySessionManager
    @Inject
    constructor(
        private val remoteProvider: AppRemoteProvider,
        private val authRepository: AuthRepository,
        private val context: Context,
        private val logger: Logger,
    ) : SessionManager {
        companion object {
            private const val TAG = "SpotifySessionManager"
        }

        private val _sessionState = MutableStateFlow<SessionState>(SessionState.Idle)
        override val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

        private val dispatcher = Dispatchers.Main
        private val scope = CoroutineScope(dispatcher)

        override suspend fun resumeSession() {
            val tokens = authRepository.getTokens().getOrNull()
            tokens?.isAccessTokenExpired(System.currentTimeMillis())?.let {
                if (!it) {
                    logger.d(TAG, "Access token stored, Connecting to remote...")
                    _sessionState.update { SessionState.ConnectingRemote }
                    connectRemote(context, CoroutineScope(Dispatchers.Main), Dispatchers.IO)
                } else {
                    _sessionState.update { SessionState.AuthorizationRequired }
                    logger.d(TAG, "No fresh access token stored")
                }
            }
        }

        override suspend fun completeAuthorization(authorizationCode: String) {
            scope.launch {
                logger.d(TAG, "Launching auth token request...")
                fetchAndStoreAuthToken(authorizationCode)
                    .onFailure { error ->
                        logger.e(TAG, "Error storing access token", error)
                        _sessionState.update {
                            SessionState.Failed(
                                SessionError.AuthFailed(error),
                            )
                        }
                    }.onSuccess {
                        logger.d(TAG, "Access token stored, Connecting to remote...")
                        connectRemote(context, scope, dispatcher)
                    }
            }
        }

        override suspend fun shutDown() {
            remoteProvider.disconnect()
            _sessionState.update { SessionState.Idle }
        }

        override suspend fun signOut() {
            authRepository.clearSessionTokens()
        }

        private fun connectRemote(
            context: Context,
            coroutineScope: CoroutineScope,
            dispatcher: CoroutineDispatcher,
        ) {
            _sessionState.update { SessionState.ConnectingRemote }
            coroutineScope.launch(dispatcher) {
                val result = remoteProvider.connect(context)
                if (result.isSuccess) {
                    logger.d(TAG, "SessionState.Ready")
                    _sessionState.update { SessionState.Ready }
                } else {
                    logger.e(TAG, "Failed to connect to remote", result.exceptionOrNull())
                    _sessionState.update {
                        SessionState.Failed(
                            SessionError.RemoteConnectionFailed(
                                result.exceptionOrNull(),
                            ),
                        )
                    }
                }
            }
        }

        private suspend fun fetchAndStoreAuthToken(authCode: String): Result<Unit> {
            val tokenResponse =
                authRepository.fetchTokenResponse(authCode).getOrElse { error ->
                    return Result.failure(error)
                }

            logger.d(TAG, "Access token fetched successfully. Storing...")
            return authRepository
                .storeTokenResponse(tokenResponse)
                .onSuccess { logger.d(TAG, "Access token successfully stored.") }
        }
    }
