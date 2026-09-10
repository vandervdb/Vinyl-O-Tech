package org.vander.spotifyclient.domain.auth

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import org.vander.spotifyclient.bridge.AuthConfigK

interface ISpotifyAuthClient {
    fun authorize(
        contextActivity: Activity,
        launcher: ActivityResultLauncher<Intent>,
        config: AuthConfigK?,
    )

    fun handleSpotifyAuthResult(
        result: ActivityResult,
        onResult: (Result<String>) -> Unit,
    )
}
