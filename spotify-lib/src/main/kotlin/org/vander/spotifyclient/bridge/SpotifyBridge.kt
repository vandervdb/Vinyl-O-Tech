package org.vander.spotifyclient.bridge

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import org.vander.core.domain.auth.IAuthRepository
import org.vander.core.domain.state.DomainPlayerState
import org.vander.core.domain.state.SessionState
import org.vander.core.logger.Logger
import org.vander.core.ui.state.UIQueueState
import org.vander.spotifyclient.bridge.util.ActivityResultFactory
import org.vander.spotifyclient.data.player.mapper.toPlayerStateDto
import org.vander.spotifyclient.domain.data.session.SpotifySessionManager
import org.vander.spotifyclient.domain.usecase.PlayerUseCase
import javax.inject.Inject

class SpotifyBridge
    @Inject
    constructor(
        private val sessionManager: SpotifySessionManager,
        private val useCase: PlayerUseCase,
        private val authRepository: IAuthRepository,
        private val appContext: Context,
        private val logger: Logger,
    ) : SpotifyBridgeApi {
        private val authTimeoutMs = 5_000L
        private val job = SupervisorJob()
        private val scope = CoroutineScope(Dispatchers.Main.immediate + job)

        private var authLauncher: ActivityResultLauncher<Intent>? = null

        private val lastState =
            MutableStateFlow(
                PlayerStateDto(
                    isPlaying = false,
                    positionMs = 0,
                    durationMs = 0,
                    trackUri = null,
                    coverId = null,
                    trackName = null,
                    artistName = null,
                    albumName = null,
                ),
            )

        override val sessionState: StateFlow<SessionState> = sessionManager.sessionState
        override val playerState: StateFlow<DomainPlayerState> = useCase.domainPlayerState
        override val uIQueueState: StateFlow<UIQueueState> = useCase.uIQueueState

        override val playerEvents: Flow<PlayerStateDto> =
            playerState
                .map { state ->
                    val dto = state.toPlayerStateDto(null)
                    logger.d(
                        TAG,
                        "playerEvents: isPlaying=${dto.isPlaying}, posMs=${dto.positionMs}, " +
                            "durMs=${dto.durationMs}, uri=${dto.trackUri}",
                    )
                    lastState.value = dto
                    dto
                }

        override fun getPlayerState(): PlayerStateDto {
            val value = lastState.value
            logger.d(
                TAG,
                "getPlayerState: isPlaying=${value.isPlaying}, " +
                    "posMs=${value.positionMs}, durMs=${value.durationMs}, uri=${value.trackUri}",
            )
            return value
        }

        override fun getSessionState(): SessionState {
            val value = sessionState.value
            logger.d(TAG, "getSessionState: $value")
            return value
        }

        override fun getUIQueueState(): UIQueueState {
            val value = uIQueueState.value
            logger.d(TAG, "getUIQueueState: $value")
            return value
        }

        /**
         * Retrieves the authentication token for the current session if available.
         *
         * This method interacts with the `authRepository` to obtain an access token, which may be null
         * if the retrieval fails or if no valid token is present.
         * This method is used by TurboModule to allow ts code to make spotify REST API calls (Playground).
         *
         * @return The authentication token as a nullable String, or null if no token is available.
         */
        override suspend fun awaitTokenOrNull(maxWaitMs: Long): String? {
            val deadline = System.currentTimeMillis() + maxWaitMs
            while (System.currentTimeMillis() < deadline) {
                authRepository.getAccessToken().getOrNull()?.let { token ->
                    if (token.isNotBlank()) return token
                }
                delay(100)
            }
            return null
        }

        override suspend fun startUpWithModuleActivityResult(
            activity: Activity,
            config: AuthConfigK?,
        ) {
            logger.d(TAG, "startUpWithModuleActivityResult(activity=$activity, config=$config)")
            val launcher = ActivityResultFactory.register(activity, createAuthCallback())
            startUp(launcher, activity, config)
        }

        override suspend fun startUpWithModuleActivityResultAndGetToken(
            activity: Activity,
            config: AuthConfigK?,
            timeoutMs: Long?,
        ): AuthResult {
            logger.d(TAG, "startUpWithModuleActivityResultAndGetToken(activity=$activity, config=$config)")
            startUpWithHostActivityResult(activity, config)
            return awaitAuthResult(
                timeoutMs ?: authTimeoutMs,
            )
        }

        override suspend fun startUpWithHostActivityResult(
            activity: Activity,
            config: AuthConfigK?,
        ) {
            logger.d(TAG, "startUpWithHostActivityResult(activity=$activity, config=$config)")
            val componentActivity =
                activity as? ComponentActivity
                    ?: error("Host activity must be a ComponentActivity")
            val launcher =
                componentActivity.activityResultRegistry.register(
                    "spotify-auth",
                    ActivityResultContracts.StartActivityForResult(),
                    createAuthCallback(),
                )
            startUp(launcher, activity, config)
        }

        override suspend fun startUpWithHostActivityResultAndGetToken(
            activity: Activity,
            config: AuthConfigK?,
            timeoutMs: Long?,
        ): AuthResult {
            logger.d(TAG, "startUpWithHostActivityResultAndGetToken(activity=$activity, config=$config)")
            startUpWithHostActivityResult(activity, config)
            return awaitAuthResult(timeoutMs ?: authTimeoutMs)
        }

        override suspend fun disconnect() {
            logger.d(TAG, "disconnect()")
            sessionManager.shutDown()
            onDestroy()
            logger.d(TAG, "disconnect() done")
        }

        override suspend fun playUri(uri: String) {
            logger.d(TAG, "playUri(uri=$uri)")
            useCase.playUri(uri)
        }

        override suspend fun pause() {
            logger.d(TAG, "pause()")
            useCase.pause()
        }

        override suspend fun resume() {
            logger.d(TAG, "resume()")
            useCase.resume()
        }

        override suspend fun seekTo(ms: Long) {
            logger.d(TAG, "seekTo(ms=$ms)")
            useCase.seekTo(ms)
        }

        override suspend fun skipNext() {
            logger.d(TAG, "skipNext()")
            useCase.skipNext()
        }

        override suspend fun skipPrevious() {
            logger.d(TAG, "skipPrevious()")
            useCase.skipPrevious()
        }

        override fun toggleSaveTrackState(trackId: String) {
            logger.d(TAG, "toggleSaveTrackState(trackId=$trackId)")
            useCase.toggleSaveTrackState(trackId)
        }

        fun onDestroy() {
            logger.d(TAG, "onDestroy() - unregister launcher + cancel job")
            authLauncher?.unregister()
            authLauncher = null
            job.cancel()
        }

        suspend fun startUp(
            launcher: ActivityResultLauncher<Intent>,
            activity: Activity,
            config: AuthConfigK?,
        ) {
            logger.d(TAG, "startUp(activity=$activity, launcher=$launcher, config=$config)")
            authLauncher = launcher

            logger.d(TAG, "startUp -> requestAuthorization()")
            sessionManager.requestAuthorization(launcher)

            logger.d(TAG, "startUp -> launchAuthorizationFlow()")
            sessionManager.launchAuthorizationFlow(activity, config)
            logger.d(TAG, "startUp() done (flow launched)")

            logger.d(TAG, "starting up PlayerUseCase")
            useCase.startUp()
        }

        suspend fun awaitAuthResult(timeout: Long = authTimeoutMs): AuthResult {
            logger.d(TAG, "filterSessionState() - filtering session state")
            return try {
                val terminal =
                    withTimeout(timeout) {
                        sessionState.first { it is SessionState.Ready || it is SessionState.Failed }
                    }

                when (terminal) {
                    is SessionState.Ready -> {
                        logger.d(TAG, "SessionState.Ready")
                        val token = awaitTokenOrNull()
                        if (token.isNullOrBlank()) {
                            AuthResult.Failed(AuthResult.Reason.TOKEN_MISSING, null)
                        } else {
                            AuthResult.Authenticated(token)
                        }
                    }

                    is SessionState.Failed -> {
                        logger.d(TAG, "SessionState.Failed - ${terminal.exception.message}")
                        AuthResult.Failed(AuthResult.Reason.SESSION_FAILED, terminal.exception)
                    }

                    else ->
                        AuthResult.Failed(
                            AuthResult.Reason.UNEXPECTED,
                            IllegalStateException("Unexpected state: $terminal"),
                        )
                }
            } catch (e: TimeoutCancellationException) {
                AuthResult.Failed(AuthResult.Reason.TIMEOUT, e)
            } catch (t: Throwable) {
                AuthResult.Failed(AuthResult.Reason.UNEXPECTED, t)
            }
        }

        private fun createAuthCallback(): (ActivityResult) -> Unit =
            { result ->
                logger.d(TAG, "createAuthCallback -> onActivityResult(result=$result)")
                sessionManager.handleAuthResult(appContext, result, scope, Dispatchers.Main)
            }

        companion object {
            private const val TAG = "SpotifyBridge"
        }
    }
