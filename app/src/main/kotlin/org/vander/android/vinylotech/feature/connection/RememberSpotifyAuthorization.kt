package org.vander.android.vinylotech.feature.connection

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.vander.core.domain.auth.AuthorizationOptions
import org.vander.core.domain.auth.AuthorizationOutcome
import org.vander.spotifyclient.data.auth.SpotifyAuthorizationContract
import org.vander.spotifyclient.domain.auth.SpotifyAuthConfig

@Composable
fun rememberSpotifyAuthorization(
    config: SpotifyAuthConfig,
    onOutcome: (AuthorizationOutcome) -> Unit,
): () -> Unit {
    val contract = remember(config) { SpotifyAuthorizationContract(config) }
    val launcher = rememberLauncherForActivityResult(contract, onOutcome)
    return remember(launcher) { { launcher.launch(AuthorizationOptions(forceApprovalDialog = true)) } }
}
