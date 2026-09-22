package org.vander.android.vinylotech.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.vander.android.vinylotech.R
import org.vander.android.vinylotech.designsystem.AndroidAppTheme
import org.vander.android.vinylotech.designsystem.VinylGradMid
import org.vander.android.vinylotech.designsystem.VinylInk
import org.vander.android.vinylotech.designsystem.VinylTextEmphasis
import org.vander.android.vinylotech.designsystem.VotDimens

// Maquette écran 02 : `padding: 14px 20px 10px`, marque 22 px bordée 2 px, point 5 px, écart 9 px.
private val BAR_PADDING_TOP = 14.dp

private val BAR_PADDING_BOTTOM = 10.dp

private val MARK_SIZE = 22.dp

private val MARK_BORDER = 2.dp

private val MARK_DOT = 5.dp

private val MARK_GAP = 9.dp

/** Glyphe à 17 px dans la maquette ; la cible tactile reste [VotDimens.touchMin]. */
private val ACTION_ICON = 20.dp

private const val MARK_FILL_ALPHA = 0.55f

/** `color: rgba(255,255,255,0.88)` sur les deux actions. */
private const val ACTION_ALPHA = 0.88f

/**
 * Barre du haut de l'écran 02, posée sur le dégradé héros : la marque et le nom à gauche,
 * recherche et réglages à droite.
 *
 * Les actions sont des `IconButton` de 48 dp, là où la maquette dessine deux glyphes de 17 px
 * sans marge : la barre y gagne un peu de hauteur, mais une cible plus petite serait sous le
 * minimum d'accessibilité.
 */
@Composable
fun VinylBrandTopBar(
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    start = VotDimens.space20,
                    end = VotDimens.space8,
                    top = BAR_PADDING_TOP,
                    bottom = BAR_PADDING_BOTTOM,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MARK_GAP),
        ) {
            BrandDot()
            Text(
                text = stringResource(R.string.brand_name),
                style = MaterialTheme.typography.titleMedium,
                color = VinylTextEmphasis,
            )
        }

        IconButton(onClick = onSearchClick, modifier = Modifier.size(VotDimens.touchMin)) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = stringResource(R.string.content_desc_search),
                tint = VinylTextEmphasis.copy(alpha = ACTION_ALPHA),
                modifier = Modifier.size(ACTION_ICON),
            )
        }
        IconButton(onClick = onSettingsClick, modifier = Modifier.size(VotDimens.touchMin)) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = stringResource(R.string.content_desc_settings),
                tint = VinylTextEmphasis.copy(alpha = ACTION_ALPHA),
                modifier = Modifier.size(ACTION_ICON),
            )
        }
    }
}

/** Version compacte de la marque, sans le bras de [VinylLogoMark] qui ne se lirait pas à 22 dp. */
@Composable
private fun BrandDot() {
    Box(
        modifier =
            Modifier
                .size(MARK_SIZE)
                .background(VinylInk.copy(alpha = MARK_FILL_ALPHA), CircleShape)
                .border(MARK_BORDER, VinylTextEmphasis, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(MARK_DOT).background(VinylTextEmphasis, CircleShape))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF7C5CFF)
@Composable
private fun VinylBrandTopBarPreview() {
    AndroidAppTheme {
        Box(Modifier.background(VinylGradMid)) {
            VinylBrandTopBar(onSearchClick = {}, onSettingsClick = {})
        }
    }
}
