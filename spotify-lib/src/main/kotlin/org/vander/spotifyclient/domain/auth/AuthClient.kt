package org.vander.spotifyclient.domain.auth

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher

/**
 * Drives the Spotify authorization flow, which runs in the Spotify app or a browser tab
 * and therefore comes back through an `onActivityResult`.
 *
 * That round trip is why the launcher is a parameter: an `ActivityResultLauncher` must be
 * registered before the Activity is STARTED, so it belongs to the Activity and can never be
 * created inside a ViewModel. This client is handed one instead of owning it.
 *
 * [handleSpotifyAuthResult] reports through a callback rather than returning, because it is
 * called from the result callback itself, outside any coroutine.
 */
interface AuthClient {
    /**
     * Launches the Spotify login screen through [launcher]. Returns immediately; the outcome
     * comes back to the Activity, which must forward it to [handleSpotifyAuthResult].
     *
     * @param config overrides client id, redirect URI and scopes; `null` falls back to the
     *   library's own constants.
     */
    fun authorize(
        contextActivity: Activity,
        launcher: ActivityResultLauncher<Intent>,
        config: AuthConfigK?,
    )

    /**
     * @param onResult receives the authorization **code** on success — not a token; exchanging
     *   it is the caller's next step.
     */
    fun handleSpotifyAuthResult(
        result: ActivityResult,
        onResult: (Result<String>) -> Unit,
    )
}
