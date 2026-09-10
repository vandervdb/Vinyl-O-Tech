package org.vander.spotifyclient.bridge.util

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

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
