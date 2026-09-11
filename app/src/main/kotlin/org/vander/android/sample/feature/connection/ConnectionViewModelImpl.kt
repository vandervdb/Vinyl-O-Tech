package org.vander.android.sample.feature.connection

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.vander.core.logger.Logger
import org.vander.core.ui.presentation.viewmodel.ConnectionViewModel
import org.vander.spotifyclient.domain.data.session.SpotifySessionManager
import javax.inject.Inject

@HiltViewModel
open class ConnectionViewModelImpl
    @Inject
    constructor(
        sessionManager: SpotifySessionManager,
        var logger: Logger,
    ) : ViewModel(),
        ConnectionViewModel {
        override val sessionState = sessionManager.sessionState
    }
