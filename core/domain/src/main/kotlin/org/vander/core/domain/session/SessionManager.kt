package org.vander.core.domain.session

import kotlinx.coroutines.flow.StateFlow
import org.vander.core.domain.state.SessionState

interface SessionManager {
    val sessionState: StateFlow<SessionState>

    suspend fun resumeSession()

    suspend fun completeAuthorization(authorizationCode: String)

    suspend fun shutDown()

    suspend fun signOut()
}
