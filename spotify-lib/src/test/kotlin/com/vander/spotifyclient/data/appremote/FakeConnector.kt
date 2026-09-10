package com.vander.spotifyclient.data.appremote

import android.content.Context
import org.vander.spotifyclient.domain.appremote.RemoteConnector

class FakeConnector : RemoteConnector {
    var listener: RemoteConnector.RemoteListener? = null
    var listArgs: Quadruple<Context, String, String, Boolean>? = null
    var disconnectedWith: Any? = null

    override fun connect(
        context: Context,
        clientId: String,
        redirectUrl: String,
        showAuthView: Boolean,
        listener: RemoteConnector.RemoteListener,
    ) {
        this.listener = listener
        this.listArgs = Quadruple(context, clientId, redirectUrl, showAuthView)
    }

    override fun disconnect(remote: Any) {
        disconnectedWith = remote
    }

    data class Quadruple<A, B, C, D>(
        val a: A,
        val b: B,
        val c: C,
        val d: D,
    )
}
