package org.vander.android.vinylotech.feature.connection

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.vander.core.logger.Logger
import org.vander.core.ui.presentation.viewmodel.ConnectionViewModel
import org.vander.spotifyclient.domain.session.SessionManager
import javax.inject.Inject

@HiltViewModel
open class SpotifyConnectionViewModel
    @Inject
    constructor(
        sessionManager: SessionManager,
        var logger: Logger,
    ) : ViewModel(),
        ConnectionViewModel {
        override val sessionState = sessionManager.sessionState
    }
