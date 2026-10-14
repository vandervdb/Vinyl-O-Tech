package org.vander.android.vinylotech.session

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.vander.core.domain.session.SessionManager
import javax.inject.Inject

/**
 * Registered on `ProcessLifecycleOwner`, whose lifecycle never reaches DESTROYED: its
 * `lifecycleScope` lives as long as the process, so `shutDown()` is not cancelled on stop.
 */
class SessionLifecycleObserver
    @Inject
    constructor(
        private val sessionManager: SessionManager,
    ) : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            owner.lifecycleScope.launch { sessionManager.resumeSession() }
        }

        override fun onStop(owner: LifecycleOwner) {
            owner.lifecycleScope.launch { sessionManager.shutDown() }
        }
    }
