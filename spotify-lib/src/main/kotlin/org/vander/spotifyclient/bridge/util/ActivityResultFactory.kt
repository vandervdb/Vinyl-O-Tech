package org.vander.spotifyclient.bridge.util

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import java.util.*

object ActivityResultFactory {
    fun register(
        activity: Activity,
        callback: (ActivityResult) -> Unit,
    ): ActivityResultLauncher<Intent> {
        val fa =
            activity as? FragmentActivity
                ?: error("Activity must extend FragmentActivity")

        val tag = "ActivityResultHolderFragment_${UUID.randomUUID()}"
        val fm = fa.supportFragmentManager

        val holder = ActivityResultHolderFragment.newInstance()
        holder.setOnActivityResult { result ->
            try {
                callback(result)
            } finally {
                if (!fa.isFinishing && !fa.isDestroyed) {
                    fa.supportFragmentManager.commit(allowStateLoss = true) {
                        remove(holder)
                    }
                }
            }
        }

        fm.commit(allowStateLoss = false) {
            add(holder, tag)
        }
        fm.executePendingTransactions()

        return holder.get()
    }
}
