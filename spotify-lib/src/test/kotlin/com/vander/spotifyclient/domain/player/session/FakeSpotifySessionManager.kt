package com.vander.spotifyclient.domain.player.session

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
        TODO("Not yet implemented")
    }

    override fun launchAuthorizationFlow(
        activity: Activity,
        config: AuthConfigK?,
    ) {
        TODO("Not yet implemented")
    }
}
