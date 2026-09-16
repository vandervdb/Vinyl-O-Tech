package org.vander.spotifyclient.domain.player.session

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.vander.core.domain.state.SessionState
import org.vander.spotifyclient.bridge.AuthConfigK
import org.vander.spotifyclient.domain.data.session.SpotifySessionManager

class FakeSpotifySessionManager : SpotifySessionManager {
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Idle)
    override val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    /** Moves the session, as the real manager does when authorization and connection progress. */
    fun emit(state: SessionState) {
        _sessionState.value = state
    }

    override fun requestAuthorization(launchAuth: ActivityResultLauncher<Intent>) {
        TODO("Not yet implemented")
    }

    override fun handleAuthResult(
        context: Context,
        result: ActivityResult,
        coroutineScope: CoroutineScope,
        dispatcher: CoroutineDispatcher,
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun shutDown() {
        _sessionState.value = SessionState.Idle
    }

    override suspend fun signout() {
        TODO("Not yet implemented")
    }

    override fun launchAuthorizationFlow(
        activity: Activity,
        config: AuthConfigK?,
    ) {
        TODO("Not yet implemented")
    }
}
