package org.vander.spotifyclient.domain.player

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher

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
