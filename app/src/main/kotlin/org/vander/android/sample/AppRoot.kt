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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.vander.android.sample.designsystem.component.VinylSnackTone
import org.vander.android.sample.designsystem.component.VinylSnackbarData
import org.vander.android.sample.designsystem.component.VinylSnackbarHost
import org.vander.android.sample.designsystem.component.rememberVinylSnackbarHostState
import org.vander.android.sample.designsystem.modifier.drawGrainOverlay
import org.vander.android.sample.navigation.AppNavHost
import org.vander.android.sample.util.LifecycleObserverComponent
import org.vander.android.sample.util.rememberSpotifySessionManager
import org.vander.core.domain.state.SessionState
import org.vander.core.logger.KermitLoggerImpl
import org.vander.core.logger.Logger

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

    // The snackbar host lives at the root so a message outlives the screen that
    // caused it. SessionState is already app-wide (SpotifySessionManager is a
    // Hilt singleton), so nothing has to be plumbed down to ConnectionScreen.
    val snack = rememberVinylSnackbarHostState()
    val sessionState by sessionManager.sessionState.collectAsStateWithLifecycle()

    // stringResource is @Composable: it cannot be called from the coroutine below.
    val failureTitle = stringResource(R.string.snack_auth_failed_title)
    val failureMessage = stringResource(R.string.snack_auth_failed_message)
    val retryLabel = stringResource(R.string.snack_retry)

    LaunchedEffect(sessionState) {
        val failure = sessionState as? SessionState.Failed ?: return@LaunchedEffect
        logger.e(tag, "authorization failed", failure.exception)
        snack.show(
            VinylSnackbarData(
                title = failureTitle,
                message = failureMessage,
                tone = VinylSnackTone.Error,
                actionLabel = retryLabel,
                onAction = onContinueWithSpotify,
            ),
        )
    }

    LifecycleObserverComponent(
        tag,
        onStopCallback = { lifecycleOwner.lifecycleScope.launch { sessionManager.shutDown() } },
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // drawGrainOverlay uses drawWithContent { drawContent(); ... }, so it paints
        // ABOVE its children whatever their order. It has to stay on the inner Box,
        // or the snackbar would end up under the grain.
        Box(
            Modifier
                .fillMaxSize()
                .drawGrainOverlay(alpha = 0.82f),
        ) {
            AppNavHost(
                navController = navController,
                contentPadding = PaddingValues(bottom = bandHeight),
                onContinueWithSpotify = onContinueWithSpotify,
                logger = logger,
            )
        }

        VinylSnackbarHost(snack, Modifier.align(Alignment.TopCenter))
    }
}
