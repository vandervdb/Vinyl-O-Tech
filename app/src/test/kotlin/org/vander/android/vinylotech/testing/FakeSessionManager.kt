package org.vander.android.vinylotech.testing

import kotlinx.coroutines.flow.MutableStateFlow
import org.vander.core.domain.session.SessionManager
import org.vander.core.domain.state.SessionState

/** Only [sessionState] is meaningful: the other members do nothing rather than pretend to work. */
class FakeSessionManager(
    initial: SessionState = SessionState.Idle,
) : SessionManager {
    override val sessionState = MutableStateFlow(initial)

    override suspend fun resumeSession() = Unit

    override suspend fun completeAuthorization(authorizationCode: String) = Unit

    override suspend fun shutDown() = Unit

    override suspend fun signOut() = Unit
}
