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
import org.vander.spotifyclient.bridge.AuthConfigK

interface SpotifySessionManager {
    val sessionState: StateFlow<SessionState>

    fun requestAuthorization(launchAuth: ActivityResultLauncher<Intent>)

    fun handleAuthResult(
        context: Context,
        result: ActivityResult,
        coroutineScope: CoroutineScope,
        dispatcher: CoroutineDispatcher = Dispatchers.Main,
    )

    suspend fun shutDown()

    fun launchAuthorizationFlow(
        activity: Activity,
        config: AuthConfigK? = null,
    )
}
