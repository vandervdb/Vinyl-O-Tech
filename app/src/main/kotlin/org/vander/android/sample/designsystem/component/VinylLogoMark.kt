package org.vander.android.sample.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import org.vander.android.sample.designsystem.AndroidAppTheme
import org.vander.android.sample.designsystem.VinylInk
import org.vander.android.sample.designsystem.VinylPurple
import org.vander.android.sample.designsystem.VotDimens

/**
 * Marque Vinyl O'Tech. `size` = diamètre réel du cercle, pas la boîte englobante :
 * le bras de lecture déborde en haut à droite d'environ 18 % du diamètre.
 *
 * À préférer au vector ic_logo_mark.xml dans un arbre Compose — le paramètre est le
 * diamètre (pas de facteur magique) et le disque est rempli, ce dont l'écran 02 a
 * besoin quand la marque se pose sur le dégradé violet.
 *
 * @param fill remplissage du disque ; Color.Transparent pour une marque évidée.
 */
@Composable
fun VinylLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = VotDimens.markSize,
    tint: Color = VinylPurple,
    fill: Color = VinylInk.copy(alpha = 0.55f),
) {
    Canvas(modifier.size(size * 1.18f)) {
        val d = this.size.minDimension / 1.18f
        val r = d / 2f
        val center = Offset(r, r + d * 0.16f)
        val ring = d * 0.047f

        drawCircle(fill, radius = r - ring / 2f, center = center)
        drawCircle(tint, radius = r - ring / 2f, center = center, style = Stroke(ring))
        drawCircle(tint.copy(alpha = 0.45f), radius = d * 0.294f, center = center, style = Stroke(d * 0.021f))
        drawCircle(tint, radius = d * 0.081f, center = center)

        // Bras de lecture : carré arrondi incliné de 18°, tangent en haut à droite
        val nub = d * 0.162f
        val nubCenter = Offset(center.x + r * 0.79f, center.y - r * 1.04f)
        rotate(18f, pivot = nubCenter) {
            drawRoundRect(
                color = tint,
                topLeft = Offset(nubCenter.x - nub / 2f, nubCenter.y - nub / 2f),
                size = Size(nub, nub),
                cornerRadius = CornerRadius(nub * 0.27f),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun VinylLogoMarkPreview() {
    AndroidAppTheme {
        VinylLogoMark()
    }
}

@Preview(showBackground = true, name = "Empty Fill")
@Composable
private fun VinylLogoMarkEmptyPreview() {
    AndroidAppTheme {
        VinylLogoMark(fill = Color.Transparent)
    }
}
