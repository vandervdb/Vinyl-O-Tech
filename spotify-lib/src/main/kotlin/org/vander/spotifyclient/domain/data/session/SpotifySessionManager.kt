package org.vander.spotifyclient.domain.data.session

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import org.vander.core.domain.state.SessionState
import org.vander.spotifyclient.domain.auth.AuthConfigK

/**
 * Orchestrates a full session: authorization first, then the App Remote connection,
 * reporting each step on [sessionState].
 *
 * The three Android types in the signatures ([Activity], [ActivityResultLauncher],
 * [ActivityResult]) are what keeps this out of a ViewModel — the authorization flow needs an
 * Activity-scoped launcher. The UI calls [requestAuthorization] and hands the result back
 * through [handleAuthResult], and observes the outcome on the flow rather than a return value.
 */
interface SpotifySessionManager {
    val sessionState: StateFlow<SessionState>

    /**
     * Hands over the launcher the UI registered, and moves [sessionState] to `Authorizing`.
     * Call it before [launchAuthorizationFlow] — without it the flow fails with
     * "Authorization flow not set".
     */
    fun requestAuthorization(launchAuth: ActivityResultLauncher<Intent>)

    suspend fun requestAuthorization(
        launchAuth: ActivityResultLauncher<Intent>,
        activity: Activity,
        context: Context,
        coroutineScope: CoroutineScope,
        dispatcher: CoroutineDispatcher,
        config: AuthConfigK?,
    )

    /**
     * Consumes the Activity result, exchanges the code for a token, then connects the App
     * Remote — the rest of the session runs from here.
     *
     * @param coroutineScope where that work runs. Supplied by the caller because this manager
     *   is a singleton and must not own a scope tied to a screen; cancelling it aborts the
     *   session mid-flight.
     * @param dispatcher used for the App Remote connection only.
     */
    fun handleAuthResult(
        context: Context,
        result: ActivityResult,
        coroutineScope: CoroutineScope,
        dispatcher: CoroutineDispatcher = Dispatchers.Main,
    )

    /**
     * Disconnects the App Remote and returns [sessionState] to `Idle`.
     *
     * The stored token is left in place, so a later start-up can reconnect without sending
     * the user back through the Spotify login screen. Use [signout] to drop it.
     */
    suspend fun shutDown()

    suspend fun signout()

    /**
     * Fires the launcher registered by [requestAuthorization]. Returns immediately; the result
     * reaches the Activity, which forwards it to [handleAuthResult].
     *
     * @param config overrides client id, redirect URI and scopes; `null` uses the library's own.
     */
    fun launchAuthorizationFlow(
        activity: Activity,
        config: AuthConfigK? = null,
    )
}
