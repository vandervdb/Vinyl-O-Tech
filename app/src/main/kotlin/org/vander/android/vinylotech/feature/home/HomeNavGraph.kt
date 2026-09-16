package org.vander.android.vinylotech.feature.home

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

fun NavGraphBuilder.homeNavGraph() {
    composable<HomeRoute> {
        val viewModel = hiltViewModel<HomeViewModelImpl>()
        HomeScreen(viewModel)
    }
}
