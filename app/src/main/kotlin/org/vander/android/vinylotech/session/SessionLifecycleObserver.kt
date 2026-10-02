package org.vander.android.vinylotech.session

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.vander.core.domain.session.SessionManager
import org.vander.spotifyclient.di.ApplicationScope
import javax.inject.Inject

class SessionLifecycleObserver
    @Inject
    constructor(
        private val sessionManager: SessionManager,
        @ApplicationScope private val scope: CoroutineScope,
    ) : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            scope.launch { sessionManager.resumeSession() }
        }

        override fun onStop(owner: LifecycleOwner) {
            scope.launch { sessionManager.shutDown() }
        }
    }
