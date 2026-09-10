package org.vander.android.sample

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.vander.android.sample.designsystem.modifier.drawGrainOverlay
import org.vander.android.sample.navigation.AppNavHost
import org.vander.android.sample.util.LifecycleObserverComponent
import org.vander.android.sample.util.rememberSpotifySessionManager
import org.vander.core.logger.KermitLoggerImpl
import org.vander.core.logger.Logger

@Suppress("FunctionNaming")
@Composable
fun AppRoot() {
    val tag = "APP"
    val logger: Logger = remember { KermitLoggerImpl(tag) }

    val navController = rememberNavController()
    val backEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backEntry?.destination?.route

    var bandHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    val activity = LocalActivity.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val sessionManager = rememberSpotifySessionManager()
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = { result ->
                activity?.let {
                    sessionManager.handleAuthResult(it, result, lifecycleOwner.lifecycleScope)
                }
            },
        )

    // Lives here, not in ConnectionScreen: the flow needs the activity result
    // launcher and the Activity. `activity` is null only outside an Activity host
    // (a preview), where the flow is meaningless — so skip instead of `!!`.
    val onContinueWithSpotify: () -> Unit = {
        activity?.let {
            sessionManager.requestAuthorization(launcher)
            sessionManager.launchAuthorizationFlow(it)
        }
    }

    LifecycleObserverComponent(
        tag,
        onStopCallback = { lifecycleOwner.lifecycleScope.launch { sessionManager.shutDown() } },
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .drawGrainOverlay(alpha = 0.82f),
    ) {
        AppNavHost(
            navController = navController,
            contentPadding = PaddingValues(bottom = bandHeight),
            onContinueWithSpotify = onContinueWithSpotify,
            logger = logger,
        )
    }
}
