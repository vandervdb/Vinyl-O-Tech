package org.vander.spotifyclient.domain.player

import kotlinx.coroutines.flow.StateFlow
import org.vander.core.domain.state.PlayerConnectionState
import org.vander.core.domain.state.PlayerStateData

/**
 * Commands sent to the Spotify player through the App Remote, plus the state it pushes back.
 *
 * Commands are `suspend` where the SDK call is asynchronous and plain functions where it is
 * fire-and-forget; none of them returns the resulting state — it comes back through
 * [subscribeToPlayerState], once the player has actually applied the change.
 *
 * [subscribeToPlayerState] and [unsubscribeFromPlayerState] must be paired: the underlying
 * SDK subscription leaks otherwise.
 */
interface PlayerClient {
    /**
     * Declared by the contract but inert today: the implementation only ever writes
     * `NotConnected`, and no caller collects it. Connection state is actually observed on
     * [org.vander.spotifyclient.domain.appremote.AppRemoteProvider.remoteState].
     */
    val playerConnectionState: StateFlow<PlayerConnectionState>

    /**
     * Last snapshot received, kept for a caller that needs a value without subscribing.
     * Nothing reads it today — [subscribeToPlayerState] is the path in use.
     */
    val lastState: StateFlow<PlayerStateData>

    /**
     * Subscribes to the player and invokes [function] on every state push.
     *
     * Note that [function] can be invoked with a state **equal** to the previous one;
     * `DefaultPlayerStateRepository` relies on that to infer a library change made elsewhere.
     * Does nothing if the App Remote is not connected.
     */
    suspend fun subscribeToPlayerState(function: (PlayerStateData) -> Unit)

    fun unsubscribeFromPlayerState()

    /**
     * @param trackUri a full `spotify:track:<id>` URI at this level, unlike the layers above
     *   which take a bare id and build the URI on the way down.
     */
    suspend fun play(trackUri: String)

    suspend fun pause()

    suspend fun resume()

    suspend fun skipNext()

    suspend fun skipPrevious()

    /** @param position absolute playback head in milliseconds. */
    fun seekTo(position: Long)

    fun setShuffle(shuffle: Boolean)

    /**
     * @param repeat the SDK's raw repeat mode, from `com.spotify.protocol.types.Repeat`:
     *   `0` OFF, `1` ONE (the current track), `2` ALL (the whole context).
     */
    fun setRepeat(repeat: Int)

    /**
     * @return the last value seen on a state push, cached locally — the player is not queried,
     *   so this is stale until the first push arrives.
     */
    fun isPlaying(): Boolean
}
