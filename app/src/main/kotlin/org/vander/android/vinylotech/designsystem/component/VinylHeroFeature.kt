package org.vander.android.vinylotech.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.vander.android.vinylotech.R
import org.vander.android.vinylotech.designsystem.AndroidAppTheme
import org.vander.android.vinylotech.designsystem.VinylOnPurple
import org.vander.android.vinylotech.designsystem.VinylPurple
import org.vander.android.vinylotech.designsystem.VinylPurpleSoft
import org.vander.android.vinylotech.designsystem.VinylTextEmphasis
import org.vander.android.vinylotech.designsystem.VinylTextOnGradient
import org.vander.android.vinylotech.designsystem.VinylTextPrimary
import org.vander.android.vinylotech.designsystem.VotDimens

// Maquette écran 02, bloc « Reprendre l'écoute » : colonne `gap: 11px`, rangée d'actions
// `gap: 12px; padding-top: 4px`, bouton `padding: 12px 22px`, glyphe ▶ 12 px, cœur 44 px.
private val BLOCK_GAP = 11.dp

private val ACTIONS_GAP = 12.dp

private val ACTIONS_TOP = 4.dp

private val LISTEN_PADDING_H = 22.dp

private val LISTEN_PADDING_V = 12.dp

private val LISTEN_GLYPH = 16.dp

private val LISTEN_GLYPH_GAP = 9.dp

private val HEART_SIZE = 44.dp

private val HEART_ICON = 18.dp

/** `box-shadow: 0 8px 30px rgba(124,92,255,0.45)`. */
private val LISTEN_ELEVATION = 12.dp

private const val LISTEN_SHADOW_ALPHA = 0.45f

/** `border: 1px solid rgba(255,255,255,0.3)` autour du cœur. */
private const val HEART_BORDER_ALPHA = 0.3f

private const val TITLE_MAX_LINES = 2

/**
 * Le bloc mis en avant en bas du héros de l'écran 02 : sur-titre, titre en très gros, ligne de
 * contexte, bouton « Écouter » et cœur.
 *
 * Ne reçoit que du texte : c'est l'appelant qui décide ce qui est mis en avant (un album, une
 * playlist) et comment on compose la ligne « artiste · année · n titres ».
 *
 * @param isSaved `null` masque le cœur, pour un élément qu'on ne peut pas enregistrer ou dont
 *   l'état n'est pas encore connu.
 */
@Composable
fun VinylHeroFeature(
    kicker: String,
    title: String,
    subtitle: String,
    isSaved: Boolean?,
    onListen: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = VotDimens.space20),
        verticalArrangement = Arrangement.spacedBy(BLOCK_GAP),
    ) {
        Text(
            text = kicker.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = VinylPurpleSoft,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            color = VinylTextEmphasis,
            maxLines = TITLE_MAX_LINES,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = VinylTextOnGradient,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            modifier = Modifier.padding(top = ACTIONS_TOP),
            horizontalArrangement = Arrangement.spacedBy(ACTIONS_GAP),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = onListen,
                modifier =
                    Modifier.shadow(
                        elevation = LISTEN_ELEVATION,
                        shape = CircleShape,
                        spotColor = VinylPurple.copy(alpha = LISTEN_SHADOW_ALPHA),
                        ambientColor = VinylPurple.copy(alpha = LISTEN_SHADOW_ALPHA),
                    ),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = VinylPurple, contentColor = VinylOnPurple),
                contentPadding = PaddingValues(horizontal = LISTEN_PADDING_H, vertical = LISTEN_PADDING_V),
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(LISTEN_GLYPH),
                )
                Text(
                    text = stringResource(R.string.home_resume_listen),
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
                    modifier = Modifier.padding(start = LISTEN_GLYPH_GAP),
                )
            }

            if (isSaved != null) {
                OutlinedIconButton(
                    onClick = onToggleSave,
                    modifier = Modifier.size(HEART_SIZE),
                    border = BorderStroke(1.dp, VinylTextEmphasis.copy(alpha = HEART_BORDER_ALPHA)),
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription =
                            stringResource(
                                if (isSaved) {
                                    R.string.content_desc_remove_from_library
                                } else {
                                    R.string.content_desc_save_to_library
                                },
                            ),
                        tint = if (isSaved) VinylPurpleSoft else VinylTextPrimary,
                        modifier = Modifier.size(HEART_ICON),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF3A2780)
@Composable
private fun VinylHeroFeaturePreview() {
    AndroidAppTheme {
        VinylHeroFeature(
            kicker = "Reprendre l'écoute",
            title = "Nuits blanches",
            subtitle = "Elia Faure · 2023 · 11 titres",
            isSaved = false,
            onListen = {},
            onToggleSave = {},
        )
    }
}
