package org.vander.android.vinylotech

import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import org.vander.android.vinylotech.designsystem.AndroidAppTheme
import org.vander.spotifyclient.utils.SpotifyAuthorizationActivity

@AndroidEntryPoint
class MainActivity : SpotifyAuthorizationActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 36) {
            setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT)
        }
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
