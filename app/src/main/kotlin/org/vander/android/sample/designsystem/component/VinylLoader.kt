package tech.vinylo.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import tech.vinylo.ui.theme.*

private const val CYCLE = 3000 // ms

/**
 * Loader « mise en lecture », cycle de 3 s en cinq temps :
 *  1. bras tangent au bord du disque
 *  2. le bras descend sur le premier sillon  (0 → 25 %)
 *  3. le disque accélère et fait deux tours  (24 → 56 %)
 *  4. il ralentit de moins en moins vite, jusqu'à l'arrêt (56 → 96 %)
 *  5. le bras remonte pendant cette décélération (78 → 95 %)
 *
 * @param size côté du carré ; le disque occupe ~85 % et le bras pivote en haut à droite.
 */
@Composable
fun VinylLoader(modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 300.dp) {
    val t = rememberInfiniteTransition(label = "loader")

    val discAngle by t.animateFloat(
        initialValue = 0f, targetValue = 720f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = CYCLE
                0f at 0 using LinearEasing
                0f at 720 using CubicBezierEasing(0.5f, 0f, 0.9f, 0.45f)  // démarrage
                160f at 1140 using LinearEasing                            // plein régime
                520f at 1680 using LinearEasing
                650f at 2040 using LinearEasing                            // paliers de plus
                700f at 2340 using LinearEasing                            // en plus serrés
                716f at 2640 using LinearEasing
                720f at 2880
            }
        ), label = "disc"
    )

    // Repos = pointe tangente au bord ; lecture = premier sillon.
    val armAngle by t.animateFloat(
        initialValue = 16f, targetValue = 16f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = CYCLE
                16f at 0
                16f at 240 using CubicBezierEasing(0.34f, 0.06f, 0.2f, 1f)
                1f at 750
                1f at 2340 using CubicBezierEasing(0.34f, 0.06f, 0.2f, 1f)
                16f at 2850
            }
        ), label = "arm"
    )

    Box(modifier.size(size)) {
        VinylDisc(
            Modifier.padding(start = size * 0.073f, top = size * 0.073f)
                .size(size * 0.853f)
                .rotate(discAngle)
        )
        // Axe central
        Canvas(Modifier.align(Alignment.Center).size(size * 0.04f)) {
            drawCircle(VinylDiscRim)
        }
        // Pivot du bras, en haut à droite
        Canvas(Modifier.align(Alignment.TopEnd).padding(end = size * 0.027f, top = size * 0.053f).size(size * 0.1f)) {
            drawCircle(Brush.linearGradient(listOf(VinylArmMetalDim, VinylArmShadow)))
        }
        Box(
            Modifier.align(Alignment.TopEnd)
                .padding(end = size * 0.05f, top = size * 0.08f)
                .width(size * 0.553f).height(size * 0.047f)
                // Le bras pivote sur son extrémité DROITE, pas sur son centre.
                .graphicsLayer {
                    rotationZ = armAngle
                    transformOrigin = TransformOrigin(1f, 0.5f)
                }
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val h = this.size.height
                // tube
                drawRect(Brush.verticalGradient(listOf(VinylArmMetal, VinylArmMetalDim)),
                    topLeft = Offset(h * 1.9f, h * 0.36f), size = Size(this.size.width - h * 2.3f, h * 0.28f))
                // tête de lecture + pointe accent
                drawRect(Brush.linearGradient(listOf(VinylArmMetal, VinylArmMetalDim)),
                    topLeft = Offset.Zero, size = Size(h * 2.1f, h))
                drawRect(VinylPurple, topLeft = Offset(h * 0.35f, h), size = Size(h * 0.21f, h * 0.57f))
                // contrepoids
                drawRect(Brush.linearGradient(listOf(VinylArmMetalDim, VinylArmShadow)),
                    topLeft = Offset(this.size.width - h * 1.6f, h * 0.07f), size = Size(h * 1.6f, h * 0.86f))
            }
        }
    }
}
