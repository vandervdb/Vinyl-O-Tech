package org.vander.spotifyclient.bridge

import android.app.Activity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.vander.core.domain.state.DomainPlayerState
import org.vander.core.domain.state.SessionState
import org.vander.core.ui.state.UIQueueState

/**
 * Facade over the whole library for a host that is not this app — a React Native
 * TurboModule, or any consumer that cannot depend on Hilt.
 *
 * Everything it exposes uses its own DTOs ([PlayerStateDto], [AuthConfigK], [AuthResult])
 * rather than the SDK or domain types, so a caller can bind to it without seeing them.
 *
 * The four `startUpWith*` entry points differ on two axes: `Module` registers its own
 * `ActivityResultLauncher` through a helper Fragment, while `Host` expects the host Activity
 * to forward the result; and the `AndGetToken` variants suspend until the token is available
 * or the given timeout elapses, instead of returning as soon as the flow is launched.
 */
interface SpotifyBridgeApi {
    val playerEvents: Flow<PlayerStateDto>
    val sessionState: StateFlow<SessionState>
    val uIQueueState: StateFlow<UIQueueState>
    val playerState: StateFlow<DomainPlayerState>

    /**
     * Current value of [playerState], for a host that cannot collect a Kotlin flow — a
     * TurboModule reading on demand from JS, typically. Prefer [playerState] otherwise.
     */
    fun getPlayerState(): PlayerStateDto

    /** Current value of [sessionState]; same rationale as [getPlayerState]. */
    fun getSessionState(): SessionState

    /** Current value of [uIQueueState]; same rationale as [getPlayerState]. */
    fun getUIQueueState(): UIQueueState

    /**
     * Waits for a token to be available, polling storage every 100 ms.
     *
     * @param maxWaitMs how long to wait before giving up.
     * @return the token, or `null` if the deadline passed. It does not start an authorization:
     *   it only observes one that is already running.
     */
    suspend fun awaitTokenOrNull(maxWaitMs: Long = 1_000): String?

    /**
     * Starts a session, the library registering its own `ActivityResultLauncher` through a
     * headless Fragment. Requires a [androidx.fragment.app.FragmentActivity].
     *
     * Returns as soon as the flow is launched — watch [sessionState] for the outcome.
     */
    suspend fun startUpWithModuleActivityResult(
        activity: Activity,
        config: AuthConfigK? = null,
    )

    /**
     * Same as [startUpWithModuleActivityResult], but suspends until the token is available.
     *
     * @param timeoutMs how long to wait; `null` uses the bridge's own default.
     * @return [AuthResult.Failed] with `TIMEOUT` when the deadline passes, which does not stop
     *   the authorization already under way.
     *
     * Note: the current implementation delegates to [startUpWithHostActivityResult], so it
     * takes the host path and requires a `ComponentActivity` rather than a `FragmentActivity`.
     */
    suspend fun startUpWithModuleActivityResultAndGetToken(
        activity: Activity,
        config: AuthConfigK? = null,
        timeoutMs: Long? = null,
    ): AuthResult

    /**
     * Starts a session, registering the launcher on the host's own
     * `activityResultRegistry` under the key `spotify-auth`. Requires a
     * [androidx.activity.ComponentActivity].
     *
     * Returns as soon as the flow is launched — watch [sessionState] for the outcome.
     */
    suspend fun startUpWithHostActivityResult(
        activity: Activity,
        config: AuthConfigK? = null,
    )

    /**
     * Same as [startUpWithHostActivityResult], but suspends until the token is available.
     *
     * @param timeoutMs how long to wait; `null` uses the bridge's own default.
     */
    suspend fun startUpWithHostActivityResultAndGetToken(
        activity: Activity,
        config: AuthConfigK? = null,
        timeoutMs: Long? = null,
    ): AuthResult

    /**
     * Ends the session: disconnects the App Remote and returns the session to `Idle`. The
     * stored token is kept, so a later start-up can reconnect without a new login —
     * dropping it is `SpotifySessionManager.signout`'s job, which nothing calls here.
     *
     * The implementation also cancels the bridge's own coroutine scope, so the instance
     * must not be reused afterwards — obtain a new one.
     */
    suspend fun disconnect()

    /**
     * @param uri a bare track id despite the name — the implementation wraps it in
     *   `SpotifyUri.track(...)`. The bridge keeps a plain `String` because its TypeScript
     *   spec cannot express the domain type; the typing starts one layer below.
     */
    suspend fun playUri(uri: String)

    suspend fun pause()

    suspend fun resume()

    /** @param ms absolute playback head in milliseconds, not a delta. */
    suspend fun seekTo(ms: Long)

    /**
     * Flips the local "saved" flag of [playerState] only. It performs no Web API call, so the
     * user's library is left untouched — saving is the host's job.
     */
    fun toggleSaveTrackState(trackId: String)

    suspend fun skipNext()

    suspend fun skipPrevious()
}
