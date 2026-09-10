package org.vander.spotifyclient.domain.appremote

import android.content.Context

interface RemoteConnector {
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
