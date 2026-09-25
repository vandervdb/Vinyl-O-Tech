package org.vander.android.vinylotech.feature.connection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import org.vander.android.vinylotech.designsystem.component.VINYL_LOADER_CYCLE_MS
import org.vander.core.domain.state.SessionState
import kotlin.time.TimeSource

@Serializable
object ConnectionRoute

fun NavGraphBuilder.connectionNavGraph(
    onContinueWithSpotify: () -> Unit,
    onSessionReady: () -> Unit,
) {
    composable<ConnectionRoute> {
        val vm: SpotifyConnectionViewModel = hiltViewModel()
        val state by vm.sessionState.collectAsStateWithLifecycle()

        val connecting = state is SessionState.Authorizing || state is SessionState.ConnectingRemote
        val spinning = rememberHeldForWholeCycles(connecting, VINYL_LOADER_CYCLE_MS)
        val readyToLeave = state is SessionState.Ready && !spinning

        LaunchedEffect(readyToLeave) {
            if (readyToLeave) onSessionReady()
        }

        ConnectionScreen(state = state, onContinueWithSpotify = onContinueWithSpotify, spinning)
    }
}

@Composable
private fun rememberHeldForWholeCycles(
    active: Boolean,
    cycleMs: Int,
): Boolean {
    var held by remember { mutableStateOf(active) }
    var startedAt by remember { mutableStateOf(TimeSource.Monotonic.markNow()) }

    LaunchedEffect(active) {
        if (active) {
            startedAt = TimeSource.Monotonic.markNow()
            held = true
            return@LaunchedEffect
        }
        if (!held) return@LaunchedEffect
        val elapsed = startedAt.elapsedNow().inWholeMilliseconds
        val whole = (elapsed / cycleMs + 1) * cycleMs
        delay(whole - elapsed)
        held = false
    }

    return held
}
