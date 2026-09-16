package org.vander.android.vinylotech.feature.home

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.vander.core.logger.Logger

@Serializable
object HomeRoute

fun NavGraphBuilder.homeNavGraph(logger: Logger) {
    composable<HomeRoute> {
        val viewmodel = hiltViewModel<HomeViewModelImpl>()
        HomeScreen(viewmodel, logger)
    }
}
