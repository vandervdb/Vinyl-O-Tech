package org.vander.android.sample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import org.vander.android.sample.designsystem.AndroidAppTheme
import org.vander.spotifyclient.utils.SpotifyAuthorizationActivity

@AndroidEntryPoint
class MainActivity : SpotifyAuthorizationActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidAppTheme {
                AppRoot()
            }
        }
    }
}
