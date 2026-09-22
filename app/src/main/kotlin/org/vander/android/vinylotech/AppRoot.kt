package org.vander.android.vinylotech

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.vander.android.vinylotech.designsystem.AndroidAppTheme
import org.vander.android.vinylotech.designsystem.VinylInk
import org.vander.android.vinylotech.designsystem.VotDimens
import org.vander.android.vinylotech.designsystem.component.VinylSnackTone
import org.vander.android.vinylotech.designsystem.component.VinylSnackbarData
import org.vander.android.vinylotech.designsystem.component.VinylSnackbarHost
import org.vander.android.vinylotech.designsystem.component.VinylSnackbarHostState
import org.vander.android.vinylotech.designsystem.component.rememberVinylSnackbarHostState
import org.vander.android.vinylotech.designsystem.modifier.drawGrainOverlay
import org.vander.android.vinylotech.feature.player.MiniPlayer
import org.vander.android.vinylotech.feature.player.PlayerViewModelImpl
import org.vander.android.vinylotech.navigation.AppNavHost
import org.vander.android.vinylotech.navigation.BottomBar
import org.vander.android.vinylotech.navigation.MainGraph
import org.vander.android.vinylotech.navigation.NavItem
import org.vander.android.vinylotech.navigation.ScreenChrome
import org.vander.android.vinylotech.navigation.navigateToTab
import org.vander.android.vinylotech.navigation.screenChrome
import org.vander.android.vinylotech.util.LifecycleObserverComponent
import org.vander.android.vinylotech.util.rememberSpotifySessionManager
import org.vander.core.domain.state.SessionState
import org.vander.core.logger.KermitLoggerImpl
import org.vander.core.logger.Logger
import org.vander.core.logger.NoOpLogger
import org.vander.fake.spotify.FakePlayerViewModel

/**
 * The app's composition root: owns the session, the NavController and the snackbar, and
 * hands [MainShell] the pieces it needs to lay out. Everything Android- or
 * Spotify-flavoured stops here, which is what lets [MainShell] be previewed.
 */
@Composable
fun AppRoot() {
    val tag = "APP_ROOT"
    val logger: Logger = remember { KermitLoggerImpl(tag) }

    val navController = rememberNavController()
    val backEntry by navController.currentBackStackEntryAsState()
    val chrome = backEntry?.destination.screenChrome()

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

    val onContinueWithSpotify: () -> Unit = {
        activity?.let {
            lifecycleOwner.lifecycleScope.launch {
                sessionManager.requestAuthorization(
                    launcher,
                    it,
                    it,
                    lifecycleOwner.lifecycleScope,
                    Dispatchers.Main,
                    null,
                )
            }
        }
    }

    val snack = rememberVinylSnackbarHostState()
    val sessionState by sessionManager.sessionState.collectAsStateWithLifecycle()

    val failureTitle = stringResource(R.string.snack_auth_failed_title)
    val failureMessage = stringResource(R.string.snack_auth_failed_message)
    val retryLabel = stringResource(R.string.snack_retry)

    LaunchedEffect(sessionState) {
        val failure = sessionState as? SessionState.Failed ?: return@LaunchedEffect
        logger.e("auth", "authorization failed", failure.exception)
        snack.show(
            VinylSnackbarData(
                title = failureTitle,
                message = failureMessage,
                tone = VinylSnackTone.Error,
                actionLabel = retryLabel,
                onAction = onContinueWithSpotify as (() -> Unit)?,
            ),
        )
    }

    LifecycleObserverComponent(
        onStopCallback = { lifecycleOwner.lifecycleScope.launch { sessionManager.shutDown() } },
    )

    MainShell(
        chrome = chrome,
        snack = snack,
        selectedTab = NavItem.all.firstOrNull { it.isSelectedIn(backEntry?.destination) },
        onTabSelected = navController::navigateToTab,
        // A slot, not a ViewModel parameter: the body below only runs when MainShell
        // actually renders the MiniPlayer, i.e. inside MainGraph. getBackStackEntry throws
        // when its route is off the back stack, so resolving it eagerly would crash on the
        // connection screen.
        miniPlayer = {
            val parentEntry = remember(backEntry) { navController.getBackStackEntry<MainGraph>() }
            // Scoped to the graph entry, not to the current destination: a destination-scoped
            // ViewModel would be cleared on every tab change, restarting the queue and the
            // spinning disc.
            val playerViewModel = hiltViewModel<PlayerViewModelImpl>(parentEntry)
            MiniPlayer(viewModel = playerViewModel, logger = logger)
        },
    ) { padding ->
        AppNavHost(
            navController = navController,
            contentPadding = padding,
            onContinueWithSpotify = onContinueWithSpotify,
            logger = logger,
        )
    }
}

/**
 * The chrome around the content: the grain overlay, the bottom band and the snackbar host.
 *
 * The band is a *sibling* of the content, not a wrapper — both are children of the same
 * Box. That is what lets the MiniPlayer survive a destination change instead of being
 * recomposed from scratch, and it is also why the band's height has to be measured by hand
 * and fed back as [PaddingValues]: no layout relationship does it for us, unlike a
 * `Scaffold`'s `innerPadding`.
 */
@Composable
private fun MainShell(
    chrome: ScreenChrome,
    snack: VinylSnackbarHostState,
    selectedTab: NavItem?,
    onTabSelected: (NavItem) -> Unit,
    miniPlayer: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    var bandHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // drawGrainOverlay draws after its children, so it covers the band too — which is
        // what the design does (band at z-index 4, grain at 5). The snackbar host stays
        // outside this Box to escape it.
        Box(
            Modifier
                .fillMaxSize()
                .drawGrainOverlay(alpha = 0.82f),
        ) {
            content(PaddingValues(bottom = bandHeight))

            if (chrome.bottomBar) {
                // Design: `position: absolute; left/right/bottom: 0; display: flex;
                // flex-direction: column; gap: 10px; padding: 10px 14px 14px; background: #0C0A10`.
                Column(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .onSizeChanged { bandHeight = with(density) { it.height.toDp() } }
                        // background BEFORE the insets: it has to cover the system navigation area
                        .background(VinylInk)
                        .navigationBarsPadding()
                        .padding(
                            start = VotDimens.dockMargin,
                            end = VotDimens.dockMargin,
                            top = VotDimens.space10,
                            bottom = VotDimens.dockMargin,
                        ),
                    verticalArrangement = Arrangement.spacedBy(VotDimens.space10),
                ) {
                    if (chrome.miniPlayer) {
                        miniPlayer()
                    }
                    BottomBar(selected = selectedTab, onSelect = onTabSelected)
                }
            }
        }

        VinylSnackbarHost(snack, Modifier.align(Alignment.TopCenter))
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewMainShell() {
    AndroidAppTheme {
        MainShell(
            chrome = ScreenChrome(bottomBar = true, miniPlayer = true),
            snack = rememberVinylSnackbarHostState(),
            selectedTab = NavItem.Home,
            onTabSelected = {},
            miniPlayer = { MiniPlayer(viewModel = FakePlayerViewModel(), logger = NoOpLogger()) },
        ) { padding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        }
    }
}
