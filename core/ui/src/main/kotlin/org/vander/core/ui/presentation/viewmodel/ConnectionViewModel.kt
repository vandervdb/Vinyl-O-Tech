package org.vander.core.ui.presentation.viewmodel

import kotlinx.coroutines.flow.StateFlow
import org.vander.core.domain.state.SessionState

/**
 * Contract for the connection screen: it only observes the session, it never drives it.
 *
 * Deliberately read-only — authorization needs an `ActivityResultLauncher`, which belongs to
 * the Activity and cannot live in a ViewModel, so it is triggered from the UI layer while
 * this flow reports the outcome.
 */
interface ConnectionViewModel {
    val sessionState: StateFlow<SessionState>
}
