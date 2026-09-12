package org.vander.spotifyclient.domain.appremote

import android.content.Context

/**
 * Thin seam over the Spotify SDK's static `SpotifyAppRemote.connect`, which is a global
 * function and therefore impossible to mock.
 *
 * Introducing this interface is what makes the connection logic unit-testable: a fake
 * connector calls [RemoteListener.onConnected] or [RemoteListener.onFailure] on demand,
 * with no device and no Spotify app installed.
 *
 * The remote is typed `Any` for the same reason — the seam must not drag the SDK types
 * into the callers.
 */
interface RemoteConnector {
    /**
     * Fire-and-forget: the outcome arrives on [listener], never as a return value.
     *
     * @param showAuthView lets the SDK open its own authorization screen when no session
     *   exists. This app passes `false` and runs the authorization itself beforehand.
     */
    fun connect(
        context: Context,
        clientId: String,
        redirectUrl: String,
        showAuthView: Boolean,
        listener: RemoteListener,
    )

    fun disconnect(remote: Any)

    interface RemoteListener {
        fun onConnected(remote: Any)

        fun onFailure(error: Throwable)
    }
}
