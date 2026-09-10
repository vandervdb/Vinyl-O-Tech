package org.vander.android.sample.feature.connection

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
object ConnectionRoute

fun NavGraphBuilder.connectionNavGraph(onContinueWithSpotify: () -> Unit) {
    composable<ConnectionRoute> {
        ConnectionScreen(onContinueWithSpotify = onContinueWithSpotify)
    }
}
