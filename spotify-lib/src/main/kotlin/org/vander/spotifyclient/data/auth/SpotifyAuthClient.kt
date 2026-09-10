package org.vander.spotifyclient.data.auth

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationClient.createLoginActivityIntent
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import org.vander.core.logger.Logger
import org.vander.spotifyclient.BuildConfig
import org.vander.spotifyclient.bridge.AuthConfigK
import org.vander.spotifyclient.domain.auth.ISpotifyAuthClient
import org.vander.spotifyclient.utils.REDIRECT_URI
import org.vander.spotifyclient.utils.SCOPE_STREAMING
import org.vander.spotifyclient.utils.USER_LIBRARY_MODIFY
import org.vander.spotifyclient.utils.USER_LIBRARY_READ
import org.vander.spotifyclient.utils.USER_READ_CURRENTLY_PLAYING
import org.vander.spotifyclient.utils.USER_READ_PLAYBACK_STATE
import org.vander.spotifyclient.utils.USER_READ_PRIVATE
import javax.inject.Inject

open class SpotifyAuthClient
    @Inject
    constructor(
        private val logger: Logger,
    ) : ISpotifyAuthClient {
        companion object {
            private const val TAG = "SpotifyClient"
        }

        // Internal representation to ease testing without Spotify SDK objects
        protected data class ParsedAuth(
            val type: Type,
            val value: String? = null,
            val error: String? = null,
        ) {
            enum class Type { TOKEN, CODE, ERROR, OTHER }
        }

        /**
         * Initiates the Spotify authorization flow.
         *
         * This function builds an [AuthorizationRequest] with the necessary client ID, redirect URI,
         * and required scopes. It then creates an [Intent] for the Spotify login activity
         * and launches it using the provided [ActivityResultLauncher].
         * The array of scopes used by the login request defines the permissions that the user
         * will be asked to grant to the app during the authorization process.
         *
         * @param contextActivity The current [Activity] context.
         * @param launcher The [ActivityResultLauncher] used to launch the Spotify login activity.
         */
        override fun authorize(
            contextActivity: Activity,
            launcher: ActivityResultLauncher<Intent>,
            config: AuthConfigK?,
        ) {
            val request =
                config?.let {
                    AuthorizationRequest
                        .Builder(
                            config.clientId,
                            AuthorizationResponse.Type.CODE,
                            config.redirectUrl,
                        ).apply {
                            setScopes(
                                config.scopes,
                            )
                            setShowDialog(config.showDialog)
                        }.build()
                } ?: AuthorizationRequest
                    .Builder(
                        BuildConfig.CLIENT_ID,
                        AuthorizationResponse.Type.CODE,
                        REDIRECT_URI,
                    ).apply {
                        setScopes(
                            arrayOf(
                                SCOPE_STREAMING,
                                USER_READ_PRIVATE,
                                USER_READ_CURRENTLY_PLAYING,
                                USER_READ_PLAYBACK_STATE,
                                USER_LIBRARY_MODIFY,
                                USER_LIBRARY_READ,
                            ),
                        )
                        setShowDialog(true)
                    }.build()
            val intent = createLoginActivityIntent(contextActivity, request)
            launcher.launch(intent)
        }

        override fun handleSpotifyAuthResult(
            result: ActivityResult,
            onResult: (Result<String>) -> Unit,
        ) {
            logger.d(TAG, "handleSpotifyAuthResult: $result")

            return if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data == null) {
                    logger.e(TAG, "No data received in activity result")
                    onResult(Result.failure<String>(Exception("No data received")))
                    return
                }
                logger.d(TAG, "Result OK")
                val parsed = parseAuthResponse(result.resultCode, data)
                when (parsed.type) {
                    ParsedAuth.Type.TOKEN -> {
                        logger.d(TAG, "Access Token received: ${parsed.value}")
                        onResult(Result.success(parsed.value ?: ""))
                    }

                    ParsedAuth.Type.CODE -> {
                        logger.d(TAG, "Authorization code received: ${parsed.value}")
                        onResult(Result.success(parsed.value ?: ""))
                    }

                    ParsedAuth.Type.ERROR -> {
                        logger.e(TAG, "Spotify Auth Error: ${parsed.error}")
                        onResult(Result.failure(Exception("Spotify Auth Error: ${parsed.error}")))
                    }

                    ParsedAuth.Type.OTHER -> {
                        logger.e(TAG, "Unexpected response type")
                        onResult(Result.failure(Exception("Unexpected response type")))
                    }
                }
            } else {
                logger.e(TAG, "Error connecting to Spotify, result code: ${result.resultCode}")
                onResult(Result.failure<String>(Exception(result.resultCode.toString())))
            }
        }

        // Extracted to make the class testable without instantiating Spotify SDK responses
        protected open fun parseAuthResponse(
            resultCode: Int,
            data: Intent,
        ): ParsedAuth {
            val response = AuthorizationClient.getResponse(resultCode, data)
            return when (response.type) {
                AuthorizationResponse.Type.TOKEN -> ParsedAuth(ParsedAuth.Type.TOKEN, value = response.accessToken)
                AuthorizationResponse.Type.CODE -> ParsedAuth(ParsedAuth.Type.CODE, value = response.code)
                AuthorizationResponse.Type.ERROR -> ParsedAuth(ParsedAuth.Type.ERROR, error = response.error)
                else -> ParsedAuth(ParsedAuth.Type.OTHER)
            }
        }
    }
