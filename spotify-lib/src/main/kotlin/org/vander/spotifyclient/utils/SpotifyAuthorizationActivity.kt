package org.vander.spotifyclient.utils

import android.os.Bundle
import androidx.activity.ComponentActivity

/**
 * Base Activity a host extends so the authorization callback lands on an Activity declared
 * by the library. `app`'s `MainActivity` extends it.
 *
 * It carries no logic of its own; the manifest declaration is the whole point. `spotify-lib`'s
 * manifest registers it as exported with a VIEW/BROWSABLE intent filter whose scheme and host
 * come from the `redirectSchemeName` and `redirectHostName` manifest placeholders. Those are
 * set in both `spotify-lib/build.gradle.kts` and `app/build.gradle.kts`, and must keep
 * matching `REDIRECT_URI` — three places to change for one value.
 */
open class SpotifyAuthorizationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}
