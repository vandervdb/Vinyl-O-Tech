package org.vander.spotifyclient.data.appremote

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import org.vander.core.domain.state.RemoteClientState
import org.vander.core.logger.Logger
import org.vander.spotifyclient.BuildConfig
import org.vander.spotifyclient.domain.appremote.AppRemoteProvider
import org.vander.spotifyclient.domain.appremote.RemoteConnector
import org.vander.spotifyclient.utils.REDIRECT_URI
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides a connection to the Spotify App Remote.
 *
 * This class is responsible for establishing and managing the connection
 * to the Spotify app on the user's device. It exposes the connection state
 * and the [SpotifyAppRemote] instance for interacting with the Spotify app.
 *
 * This class is a Singleton, meaning there will only be one instance of it
 * throughout the application's lifecycle.
 */
@Singleton
class SpotifyAppRemoteProvider
    @Inject
    constructor(
        private val connector: RemoteConnector,
        private val logger: Logger,
    ) : AppRemoteProvider {
        companion object {
            private const val TAG = "SpotifyAppRemoteProvider"
        }

        private var _remoteState =
            MutableStateFlow<RemoteClientState>(RemoteClientState.NotConnected)
        override val remoteState: StateFlow<RemoteClientState> = _remoteState.asStateFlow()

        private var remoteHandle: Any? = null

        override suspend fun connect(context: Context): Result<Unit> {
            _remoteState.update { RemoteClientState.Connecting }

            return suspendCancellableCoroutine { continuation ->

                connector.connect(
                    context,
                    BuildConfig.CLIENT_ID,
                    REDIRECT_URI,
                    false,
                    object : RemoteConnector.RemoteListener {
                        override fun onConnected(remote: Any) {
                            logger.d(TAG, "Connected to remote")
                            // Store the raw handle without forcing the SpotifyAppRemote type.
                            // This keeps tests (which use a plain Any) safe and still allows
                            // production code to retrieve the typed handle via get().
                            remoteHandle = remote
                            _remoteState.update { RemoteClientState.Connected }
                            continuation.resumeWith(Result.success(Result.success(Unit)))
                        }

                        override fun onFailure(error: Throwable) {
                            _remoteState.update { RemoteClientState.Failed(error as Exception) }
                            logger.e(TAG, "SpotifyAppRemote connection failed", error)
                            continuation.resumeWith(Result.success(Result.failure(error)))
                        }
                    },
                )
            }
        }

        override fun get(): com.spotify.android.appremote.api.SpotifyAppRemote? =
            (remoteHandle as? com.spotify.android.appremote.api.SpotifyAppRemote)

        override fun getRemoteHandle(): Any? = remoteHandle

        override fun disconnect() {
            remoteHandle?.let { handle ->
                try {
                    connector.disconnect(handle)
                    _remoteState.update { RemoteClientState.NotConnected }
                } catch (t: Throwable) {
                    logger.e(TAG, "Connector disconnect failed", t)
                    _remoteState.update { RemoteClientState.Failed(t as Exception) }
                }
            }

            remoteHandle = null
            _remoteState.update { RemoteClientState.NotConnected }
        }
    }
