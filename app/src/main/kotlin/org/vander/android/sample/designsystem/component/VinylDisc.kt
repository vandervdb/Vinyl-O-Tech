package tech.vinylo.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import tech.vinylo.ui.theme.*

/**
 * Disque vinyle : sillons concentriques + label accent.
 * Rendu 2D plat, aucun relief simulé — même vocabulaire que les maquettes.
 * @param labelColor couleur du label central (par défaut l'accent ; passer la couleur
 *                   dominante de la pochette pour les vignettes de collection).
 */
@Composable
fun VinylDisc(modifier: Modifier = Modifier, labelColor: Color = VinylPurple) {
    // Stops en fraction du rayon, repris à l'identique de la maquette.
    val stops = arrayOf(
        0.000f to VinylDiscHole, 0.064f to VinylDiscHole,
        0.066f to labelColor, 0.200f to labelColor,
        0.204f to VinylPurpleDim, 0.214f to VinylPurpleDim,
        0.220f to VinylDiscMid, 0.280f to VinylDiscMid,
        0.284f to VinylDiscGroove, 0.300f to VinylDiscGroove,
        0.304f to VinylDiscBase, 0.400f to VinylDiscBase,
        0.404f to VinylDiscGroove, 0.416f to VinylDiscGroove,
        0.420f to VinylDiscBase, 0.520f to VinylDiscBase,
        0.524f to VinylDiscGrooveHi, 0.536f to VinylDiscGrooveHi,
        0.540f to VinylDiscBase, 0.640f to VinylDiscBase,
        0.644f to VinylDiscGroove, 0.656f to VinylDiscGroove,
        0.660f to VinylDiscBase, 0.760f to VinylDiscBase,
        0.764f to VinylDiscGrooveHi, 0.776f to VinylDiscGrooveHi,
        0.780f to VinylDiscBase, 0.880f to VinylDiscBase,
        0.884f to VinylDiscRim, 0.900f to VinylDiscRim,
        0.904f to VinylDiscDeep, 0.964f to VinylDiscDeep,
        0.968f to VinylDiscRim, 1.000f to VinylDiscRim,
    )
    Canvas(modifier) {
        val r = size.minDimension / 2f
        val c = Offset(size.width / 2f, size.height / 2f)
        drawCircle(Brush.radialGradient(*stops, center = c, radius = r), radius = r, center = c)
        // Reflet doux, fixe par rapport au disque (il tourne donc avec lui)
        drawCircle(
            Brush.sweepGradient(
                0.00f to Color.Transparent, 0.11f to Color.White.copy(alpha = 0.16f),
                0.26f to Color.Transparent, 0.56f to Color.Transparent,
                0.67f to Color.White.copy(alpha = 0.10f), 0.81f to Color.Transparent,
                center = c
            ), radius = r, center = c
        )
        drawCircle(VinylDiscHole, radius = r * 0.03f, center = c)
    }
}
