package org.vander.core.ui.presentation.viewmodel

import kotlinx.coroutines.flow.StateFlow
import org.vander.core.domain.state.SessionState

interface ConnectionViewModel {
    val sessionState: StateFlow<SessionState>
}
