package org.vander.android.sample.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.ui.graphics.vector.ImageVector
import org.vander.android.sample.R
import org.vander.android.sample.feature.home.HomeRoute
import org.vander.android.sample.feature.library.SpotifyRoute
import kotlin.reflect.KClass

sealed class NavItem(
    val route: KClass<*>,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    companion object {
        val all = listOf(Home, Spotify)
    }

    object Home : NavItem(HomeRoute::class, R.string.nav_label_home, Icons.Filled.Home, Icons.Outlined.Home)

    object Spotify :
        NavItem(
            SpotifyRoute::class,
            R.string.nav_label_spotify,
            Icons.Filled.LibraryMusic,
            Icons.Outlined.LibraryMusic,
        )
}
