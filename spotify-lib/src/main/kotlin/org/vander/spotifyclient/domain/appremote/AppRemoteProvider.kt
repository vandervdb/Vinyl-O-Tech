package org.vander.spotifyclient.domain.appremote

import android.content.Context
import com.spotify.android.appremote.api.SpotifyAppRemote
import kotlinx.coroutines.flow.StateFlow
import org.vander.core.domain.state.RemoteClientState

interface AppRemoteProvider {
    val remoteState: StateFlow<RemoteClientState>

    suspend fun connect(context: Context): Result<Unit>

    fun get(): SpotifyAppRemote?

    fun disconnect()

    fun getRemoteHandle(): Any?
}
