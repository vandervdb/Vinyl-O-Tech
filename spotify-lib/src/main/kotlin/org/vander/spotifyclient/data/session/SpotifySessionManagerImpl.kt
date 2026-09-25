package org.vander.spotifyclient.data.session

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.vander.core.domain.auth.IAuthRepository
import org.vander.core.domain.error.SessionError
import org.vander.core.domain.state.SessionState
import org.vander.core.logger.Logger
import org.vander.spotifyclient.domain.appremote.AppRemoteProvider
import org.vander.spotifyclient.domain.auth.AuthClient
import org.vander.spotifyclient.domain.auth.AuthConfigK
import org.vander.spotifyclient.domain.data.session.SpotifySessionManager
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
class SpotifySessionManagerImpl
    @Inject
    constructor(
        private val authClient: AuthClient,
        private val remoteProvider: AppRemoteProvider,
        private val authRepository: IAuthRepository,
        private val logger: Logger,
    ) : SpotifySessionManager {
        companion object {
            private const val TAG = "SpotifySessionManagerImpl"
        }

        private val _sessionState = MutableStateFlow<SessionState>(SessionState.Idle)
        override val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

        private var launchAuthFlow: ActivityResultLauncher<Intent>? = null

        override fun requestAuthorization(launchAuth: ActivityResultLauncher<Intent>) {
            logger.d(TAG, "Requesting authorization...")
            launchAuthFlow = launchAuth
            _sessionState.update { SessionState.Authorizing }
        }

        override suspend fun requestAuthorization(
            launchAuth: ActivityResultLauncher<Intent>,
            activity: Activity,
            context: Context,
            coroutineScope: CoroutineScope,
            dispatcher: CoroutineDispatcher,
            config: AuthConfigK?,
        ) {
            logger.d(TAG, "Requesting authorization...")
            launchAuthFlow = launchAuth
            _sessionState.update { SessionState.Authorizing }

            coroutineScope.launch {
                authRepository
                    .getAccessToken()
                    .onSuccess { token ->
                        if (token.isNotBlank()) {
                            logger.d(TAG, "Access token stored, Connecting to remote...")
                            connectRemote(context, coroutineScope, dispatcher)
                        } else {
                            launchAuthorizationFlow(activity, config)
                        }
                    }.onFailure { error ->
                        logger.e(TAG, "Error checking access token", error)
                        launchAuthorizationFlow(activity, config)
                    }
            }
        }

        override fun launchAuthorizationFlow(
            activity: Activity,
            config: AuthConfigK?,
        ) {
            logger.d(TAG, "Launching authorization flow with launcher: $launchAuthFlow")
            try {
                launchAuthFlow?.let {
                    logger.d(TAG, "Calling authClient.authorize")
                    authClient.authorize(activity, it, config)
                } ?: run {
                    _sessionState.update {
                        SessionState.Failed(
                            SessionError.UnknownError(
                                Exception("Authorization flow not set"),
                            ),
                        )
                    }
                }
            } catch (e: Exception) {
                logger.e(TAG, "Error in launchAuthorizationFlow", e)
                _sessionState.update {
                    SessionState.Failed(SessionError.UnknownError(e))
                }
            }
        }

        override fun handleAuthResult(
            context: Context,
            result: ActivityResult,
            coroutineScope: CoroutineScope,
            dispatcher: CoroutineDispatcher,
        ) {
            logger.d(TAG, "handleAuthResult called with result: $result")
            authClient.handleSpotifyAuthResult(result) { authResult ->
                logger.d(TAG, "authClient.handleSpotifyAuthResult callback: success=${authResult.isSuccess}")
                if (authResult.isSuccess) {
                    coroutineScope.launch {
                        val authCode = authResult.getOrElse { "" }
                        logger.d(TAG, "Launching auth token request...")
                        fetchAndStoreAuthToken(authCode)
                            .onFailure { error ->
                                logger.e(TAG, "Error storing access token", error)
                                _sessionState.update {
                                    SessionState.Failed(
                                        SessionError.AuthFailed(error),
                                    )
                                }
                            }.onSuccess {
                                logger.d(TAG, "Access token stored, Connecting to remote...")
                                connectRemote(context, coroutineScope, dispatcher)
                            }
                    }
                } else {
                    _sessionState.update {
                        SessionState.Failed(
                            SessionError.AuthFailed(
                                Exception("Authorization failed with unknown error"),
                            ),
                        )
                    }
                }
            }
        }

        override suspend fun shutDown() {
            remoteProvider.disconnect()
            _sessionState.update { SessionState.Idle }
        }

        override suspend fun signout() {
            authRepository.clearAccessToken()
        }

        private fun connectRemote(
            context: Context,
            coroutineScope: CoroutineScope,
            dispatcher: CoroutineDispatcher = Dispatchers.Main,
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
