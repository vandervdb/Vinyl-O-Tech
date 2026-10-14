package org.vander.android.vinylotech

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import org.vander.android.vinylotech.session.SessionLifecycleObserver
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application() {
    @Inject
    lateinit var sessionLifecycleObserver: SessionLifecycleObserver

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(sessionLifecycleObserver)
    }
}
