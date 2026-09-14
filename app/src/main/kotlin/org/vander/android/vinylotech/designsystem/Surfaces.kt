package org.vander.android.vinylotech.designsystem

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/*
 * Équivalents Compose des drawables View-system que painterResource refuse
 * (layer-list, shape, selector). Les XML correspondants ont été retirés du kit :
 * ces fonctions sont la version canonique.
 *
 * GrainOverlay() du kit a été écarté : il étire la texture (ContentScale.FillBounds)
 * au lieu de la tuiler et n'applique pas BlendMode.Overlay. Utiliser
 * Modifier.drawGrainOverlay() (ui/util/ModifierDrawGrainOverlay.kt) à la place.
 */

/** Dégradé héros de l'accueil — remplace bg_hero_gradient.xml. 155° ≈ diagonale descendante. */
fun heroGradient(
    widthPx: Float,
    heightPx: Float,
) = Brush.linearGradient(
    0.00f to VinylGradStart,
    0.42f to VinylGradMid,
    1.00f to VinylGradEnd,
    start = Offset(widthPx * 0.78f, 0f),
    end = Offset(widthPx * 0.22f, heightPx),
)

/** Fondu du héros vers le fond d'écran, pour garder titre et boutons lisibles. */
val HeroScrim =
    Brush.verticalGradient(
        0.00f to VinylInk.copy(alpha = 0.35f),
        0.30f to Color.Transparent,
        0.78f to VinylInk.copy(alpha = 0.86f),
        1.00f to VinylInk,
    )

/**
 * Halo violet de l'écran de connexion : 2 arrêts, coupé à 68 % du rayon.
 * Valeurs de la maquette — ne pas adoucir le falloff, le halo doit rester net.
 */
fun loginHalo(radiusPx: Float) =
    Brush.radialGradient(
        0.00f to VinylPurple.copy(alpha = 0.42f),
        0.68f to Color.Transparent,
        radius = radiusPx,
    )

/** Chip de filtre — remplace bg_chip_filter.xml. */
fun Modifier.filterChip(selected: Boolean): Modifier =
    if (selected) {
        background(VinylTextEmphasis, VotShapes.small)
    } else {
        background(VinylInk.copy(alpha = 0.34f), VotShapes.small)
    }

/** Dock flottant du mini-player — remplace bg_dock.xml. */
fun Modifier.dockSurface(): Modifier = background(VinylSurfaceHigh, VotShapes.medium)
