package org.vander.android.vinylotech.designsystem.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.vander.android.vinylotech.designsystem.AndroidAppTheme
import org.vander.android.vinylotech.designsystem.VinylGradEnd
import org.vander.android.vinylotech.designsystem.VinylOutlineStrong
import org.vander.android.vinylotech.designsystem.VinylTextEmphasis
import org.vander.android.vinylotech.designsystem.VotDimens
import org.vander.android.vinylotech.designsystem.VotShapes
import org.vander.android.vinylotech.designsystem.filterChip

// Maquette écran 02 : chip actif `padding: 7px 14px`, inactif `6px 13px` + bordure 1 px — les
// deux font la même taille, la bordure mange le pixel retiré.
private val CHIP_PADDING_H = 14.dp

private val CHIP_PADDING_V = 7.dp

private val CHIP_BORDER = 1.dp

/**
 * Rangée de filtres de l'écran 02 (« Tout · Albums · Playlists · Artistes »).
 *
 * Générique sur le type d'option : le composant ne sait ni ce qu'est un filtre, ni d'où vient
 * son libellé, ce qui le garde dans le design system. Défile horizontalement quand les libellés
 * traduits ne tiennent plus sur la largeur.
 */
@Composable
fun <T> VinylFilterChipRow(
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()).padding(horizontal = VotDimens.space20),
        horizontalArrangement = Arrangement.spacedBy(VotDimens.space8),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Box(
                modifier =
                    Modifier
                        .clip(VotShapes.small)
                        .filterChip(isSelected)
                        .then(
                            if (isSelected) {
                                Modifier
                            } else {
                                Modifier.border(
                                    CHIP_BORDER,
                                    VinylOutlineStrong,
                                    VotShapes.small,
                                )
                            },
                        ).semantics { this.selected = isSelected }
                        .clickable(role = Role.Tab) { onSelect(option) }
                        // The border eats one pixel of padding, so both states keep the same size.
                        .padding(
                            horizontal = if (isSelected) CHIP_PADDING_H else CHIP_PADDING_H - CHIP_BORDER,
                            vertical = if (isSelected) CHIP_PADDING_V else CHIP_PADDING_V - CHIP_BORDER,
                        ),
            ) {
                Text(
                    text = label(option),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) VinylGradEnd else VinylTextEmphasis,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF7C5CFF)
@Composable
private fun VinylFilterChipRowPreview() {
    AndroidAppTheme {
        VinylFilterChipRow(
            options = listOf("Tout", "Albums", "Playlists", "Artistes"),
            selected = "Tout",
            label = { it },
            onSelect = {},
        )
    }
}
