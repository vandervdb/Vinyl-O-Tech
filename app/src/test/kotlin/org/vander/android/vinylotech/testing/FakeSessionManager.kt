package org.vander.android.vinylotech.testing

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import org.vander.core.domain.state.SessionState
import org.vander.spotifyclient.bridge.AuthConfigK
import org.vander.spotifyclient.domain.data.session.SpotifySessionManager

/**
 * Only [sessionState] is meaningful. The authorization members take Android types that are
 * stubs on the JVM, so they do nothing rather than pretend to work.
 */
class FakeSessionManager(
    initial: SessionState = SessionState.Idle,
) : SpotifySessionManager {
    override val sessionState = MutableStateFlow(initial)

    override fun requestAuthorization(launchAuth: ActivityResultLauncher<Intent>) = Unit

    override fun handleAuthResult(
        context: Context,
        result: ActivityResult,
        coroutineScope: CoroutineScope,
        dispatcher: CoroutineDispatcher,
    ) = Unit

    override suspend fun shutDown() = Unit

    override suspend fun signout() = Unit

    override fun launchAuthorizationFlow(
        activity: Activity,
        config: AuthConfigK?,
    ) = Unit
}
