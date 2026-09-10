package org.vander.android.sample.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.vander.android.sample.R
import org.vander.android.sample.feature.connection.ConnectionScreen

/**
 * Destination behind the "Accueil" tab — screen 02 of the Vinyl O'Tech design
 * (purple hero header, filter chips, "Ta bibliothèque" grid). Not built yet, so
 * this is an explicit placeholder.
 *
 * It used to delegate to [ConnectionScreen], which was left over from when both
 * files carried screen 01. That is now wrong: the login screen is its own
 * destination ([org.vander.android.sample.ui.navigation.ConnectionRoute]) and is
 * not a tab, so the Accueil tab would have shown the login screen.
 */
@Suppress("FunctionNaming")
@Composable
fun HomeScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.home_placeholder),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
