package org.vander.android.vinylotech

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.vander.core.domain.auth.AuthorizationOutcome
import org.vander.core.domain.session.SessionManager
import org.vander.core.logger.Logger
import org.vander.core.ui.presentation.viewmodel.ConnectionViewModel
import javax.inject.Inject

@HiltViewModel
class SpotifySessionViewModel
    @Inject
    constructor(
        private val sessionManager: SessionManager,
        private val logger: Logger,
    ) : ViewModel(),
        ConnectionViewModel {
        override val sessionState = sessionManager.sessionState

        override fun onSessionOutcome(outcome: AuthorizationOutcome) {
            when (outcome) {
                is AuthorizationOutcome.Code -> {
                    viewModelScope.launch {
                        sessionManager.completeAuthorization(outcome.value)
                    }
                }

                is AuthorizationOutcome.Cancelled -> {
                    logger.i(TAG, "Authorization cancelled. Do Nothing")
                }

                is AuthorizationOutcome.Failed -> {
                    logger.e(TAG, "Authorization failed", Exception(outcome.reason))
                }
            }
        }

        companion object {
            private const val TAG = "SpotifySessionViewModel"
        }
    }
