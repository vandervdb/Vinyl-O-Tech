package org.vander.spotifyclient.data.appremote

import android.content.Context
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import org.vander.spotifyclient.domain.appremote.RemoteConnector
import org.vander.spotifyclient.utils.REDIRECT_URI

class SpotifyRemoteConnector : RemoteConnector {
    override fun connect(
        context: Context,
        clientId: String,
        redirectUrl: String,
        showAuthView: Boolean,
        listener: RemoteConnector.RemoteListener,
    ) {
        val params =
            ConnectionParams
                .Builder(clientId)
                .setRedirectUri(REDIRECT_URI)
                .showAuthView(showAuthView)
                .build()

        SpotifyAppRemote.connect(
            context,
            params,
            object : Connector.ConnectionListener {
                override fun onConnected(remote: SpotifyAppRemote) {
                    listener.onConnected(remote)
                }

                override fun onFailure(p0: Throwable?) {
                    listener.onFailure(p0!!)
                }
            },
        )
    }

    override fun disconnect(remote: Any) {
        (remote as? SpotifyAppRemote).let { SpotifyAppRemote.disconnect(it) }
    }
}
