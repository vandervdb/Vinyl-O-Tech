package org.vander.spotifyclient.domain.player

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher

/**
 * Unused duplicate of [org.vander.spotifyclient.domain.auth.ISpotifyAuthClient], kept here
 * without an `AuthConfigK` parameter. No file imports this one — the `domain.auth` version
 * is the live contract.
 */
interface ISpotifyAuthClient {
    fun authorize(
        contextActivity: Activity,
        launcher: ActivityResultLauncher<Intent>,
    )

    fun handleSpotifyAuthResult(
        result: ActivityResult,
        onResult: (Result<String>) -> Unit,
    )
}
