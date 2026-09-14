package org.vander.android.vinylotech.feature.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

fun NavGraphBuilder.homeNavGraph() {
    composable<HomeRoute> {
        HomeScreen()
    }
}
