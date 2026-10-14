package org.vander.spotifyclient.domain.player.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.vander.core.domain.session.SessionManager
import org.vander.core.domain.state.SessionState

class FakeSessionManager : SessionManager {
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Idle)
    override val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    /** Moves the session, as the real manager does when authorization and connection progress. */
    fun emit(state: SessionState) {
        _sessionState.value = state
    }

    override suspend fun resumeSession() {
        TODO("Not yet implemented")
    }

    override suspend fun completeAuthorization(authorizationCode: String) {
        TODO("Not yet implemented")
    }

    override suspend fun shutDown() {
        _sessionState.value = SessionState.Idle
    }

    override suspend fun signOut() {
        TODO("Not yet implemented")
    }
}
