package org.vander.android.sample.feature.connection

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
object ConnectionRoute

fun NavGraphBuilder.connectionNavGraph(onContinueWithSpotify: () -> Unit) {
    composable<ConnectionRoute> {
        val vm: ConnectionViewModelImpl = hiltViewModel()
        val state by vm.sessionState.collectAsStateWithLifecycle()
        ConnectionScreen(state = state, onContinueWithSpotify = onContinueWithSpotify)
    }
}
