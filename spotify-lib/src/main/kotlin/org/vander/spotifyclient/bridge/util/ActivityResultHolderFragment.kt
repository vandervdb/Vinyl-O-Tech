package org.vander.spotifyclient.bridge.util

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

/**
 * Headless Fragment whose only job is to own an `ActivityResultLauncher` with a proper
 * lifecycle. It has no view and is added, used, then removed by [ActivityResultFactory].
 *
 * The registration is duplicated in [onCreate] and in [get], guarded by `isInitialized`, so
 * the launcher exists whichever happens first.
 */
class ActivityResultHolderFragment : Fragment() {
    private var onActivityResult: ((ActivityResult) -> Unit)? = null
    private lateinit var launcher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!::launcher.isInitialized) {
            launcher =
                registerForActivityResult(
                    ActivityResultContracts.StartActivityForResult(),
                ) { result ->
                    onActivityResult?.invoke(result)
                }
        }
    }

    fun setOnActivityResult(cb: (ActivityResult) -> Unit) {
        this.onActivityResult = cb
    }

    fun get(): ActivityResultLauncher<Intent> {
        if (!::launcher.isInitialized) {
            launcher =
                registerForActivityResult(
                    ActivityResultContracts.StartActivityForResult(),
                ) { result ->
                    onActivityResult?.invoke(result)
                }
        }
        return launcher
    }

    companion object {
        fun newInstance() = ActivityResultHolderFragment()
    }
}
