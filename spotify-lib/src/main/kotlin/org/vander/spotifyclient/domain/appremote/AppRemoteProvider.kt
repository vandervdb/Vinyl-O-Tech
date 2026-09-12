package org.vander.spotifyclient.domain.appremote

import android.content.Context
import com.spotify.android.appremote.api.SpotifyAppRemote
import kotlinx.coroutines.flow.StateFlow
import org.vander.core.domain.state.RemoteClientState

/**
 * Owns the `SpotifyAppRemote` connection and its lifecycle.
 *
 * [get] returns `null` while disconnected, so a caller must always handle the absence of a
 * remote rather than assume [connect] succeeded.
 *
 * [getRemoteHandle] returns `Any?` on purpose: it lets a consumer that must not depend on
 * the Spotify SDK types pass the handle along opaquely.
 */
interface AppRemoteProvider {
    val remoteState: StateFlow<RemoteClientState>

    /**
     * Connects to the Spotify app, moving [remoteState] to `Connecting` then `Connected` or
     * `Failed`. Suspends until the SDK answers, with no timeout of its own.
     *
     * @return failure when the Spotify app is missing, not logged in, or refuses the
     *   connection; the cause is also published on [remoteState].
     */
    suspend fun connect(context: Context): Result<Unit>

    fun get(): SpotifyAppRemote?

    fun disconnect()

    fun getRemoteHandle(): Any?
}
