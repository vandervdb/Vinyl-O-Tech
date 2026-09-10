package org.vander.android.sample.util

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
@Suppress("FunctionNaming")
fun LifecycleObserverComponent(
    tag: String? = null,
    onStartCallback: (() -> Unit)? = null,
    onStopCallback: (() -> Unit)? = null,
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> {
                        tag?.let { Log.d(tag, "Visible on screen") }
                        onStartCallback?.invoke()
                    }

                    Lifecycle.Event.ON_STOP -> {
                        tag?.let { Log.d(tag, "Hidden (but not yet destroyed)") }
                        onStopCallback?.invoke()
                    }

                    Lifecycle.Event.ON_DESTROY -> {
                        tag?.let { Log.d(tag, "Destroyed") }
                        onStopCallback?.invoke()
                    }

                    else -> {
                        // should not happen
                    }
                }
            }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
