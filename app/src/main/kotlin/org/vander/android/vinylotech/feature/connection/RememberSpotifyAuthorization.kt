package org.vander.android.vinylotech.feature.connection

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.vander.core.domain.auth.AuthorizationOptions
import org.vander.core.domain.auth.AuthorizationOutcome

@Composable
fun rememberSpotifyAuthorization(
    contract: ActivityResultContract<AuthorizationOptions, AuthorizationOutcome>,
    onOutcome: (AuthorizationOutcome) -> Unit,
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(contract, onOutcome)
    return remember(launcher) { { launcher.launch(AuthorizationOptions(forceApprovalDialog = true)) } }
}
