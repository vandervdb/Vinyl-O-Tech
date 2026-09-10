package org.vander.android.sample.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource

@Composable
fun BottomBar(navItems: List<NavItem> = NavItem.all) {
    var selectedItem by remember { mutableStateOf(0) }
    ShortNavigationBar {
        navItems.forEachIndexed { index, item ->
            ShortNavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selectedItem == index) item.selectedIcon else item.unselectedIcon,
                        contentDescription = stringResource(item.labelRes),
                    )
                },
                label = { Text(stringResource(item.labelRes)) },
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                },
            )
        }
    }
}
