package org.vander.android.sample

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import org.vander.android.sample.designsystem.AndroidAppTheme
import org.vander.spotifyclient.utils.SpotifyAuthorizationActivity

@AndroidEntryPoint
class MainActivity : SpotifyAuthorizationActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            AndroidAppTheme {
                AppRoot()
            }
        }
    }
}
