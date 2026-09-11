package org.vander.android.sample.designsystem.component

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.vander.android.sample.designsystem.AndroidAppTheme
import org.vander.android.sample.designsystem.VinylArmMetal
import org.vander.android.sample.designsystem.VinylArmMetalDim
import org.vander.android.sample.designsystem.VinylArmShadow
import org.vander.android.sample.designsystem.VinylDiscRim
import org.vander.android.sample.designsystem.VinylPurple

/**
 * Durée d'un cycle complet, en ms. Publique parce qu'un appelant qui veut garantir
 * une animation « entière » doit pouvoir s'aligner dessus : à la fin du cycle le
 * disque est revenu à 0° et le bras à sa position de repos, donc couper l'animation
 * à ce moment précis ne produit aucun saut visuel.
 */
const val VINYL_LOADER_CYCLE_MS = 3000

private const val CYCLE = VINYL_LOADER_CYCLE_MS

/** Bras tangent au bord du disque : angle de repos, et première keyframe du cycle. */
private const val ARM_REST_ANGLE = 16f

/** Disque à l'arrêt : le cycle boucle sur 720°, soit un tour complet, donc 0° est aussi sa fin. */
private const val DISC_REST_ANGLE = 0f

/**
 * Loader « mise en lecture », cycle de 3 s en cinq temps :
 *  1. bras tangent au bord du disque
 *  2. le bras descend sur le premier sillon  (0 → 25 %)
 *  3. le disque accélère et fait deux tours  (24 → 56 %)
 *  4. il ralentit de moins en moins vite, jusqu'à l'arrêt (56 → 96 %)
 *  5. le bras remonte pendant cette décélération (78 → 95 %)
 *
 * @param size côté du carré ; le disque occupe ~85 % et le bras pivote en haut à droite.
 * @param grooveText texte gravé le long d'un sillon, transmis tel quel à [VinylDisc].
 * @param animated `false` fige la platine au repos — bras relevé, disque immobile — et ne compose
 *   aucune animation, donc plus aucune frame n'est redessinée. Repasser à `true` relance le cycle
 *   depuis son début.
 */
@Composable
fun VinylLoader(
    modifier: Modifier = Modifier,
    size: Dp = 300.dp,
    animated: Boolean = true,
    grooveText: String? = null,
) {
    val angles =
        if (animated) {
            rememberLoaderAngles()
        } else {
            LoaderAngles(disc = DISC_REST_ANGLE, arm = ARM_REST_ANGLE)
        }

    Box(modifier.size(size)) {
        VinylDisc(
            modifier =
                Modifier
                    .padding(start = size * 0.073f, top = size * 0.073f)
                    .size(size * 0.853f)
                    .rotate(angles.disc),
            grooveText = grooveText,
        )
        // Axe central
        Canvas(
            Modifier
                .align(Alignment.Center)
                .size(size * 0.04f),
        ) {
            drawCircle(VinylDiscRim)
        }
        // Pivot du bras, en haut à droite
        Canvas(
            Modifier
                .align(Alignment.TopEnd)
                .padding(end = size * 0.027f, top = size * 0.053f)
                .size(size * 0.1f),
        ) {
            drawCircle(Brush.linearGradient(listOf(VinylArmMetalDim, VinylArmShadow)))
        }
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(end = size * 0.05f, top = size * 0.08f)
                .width(size * 0.553f)
                .height(size * 0.047f)
                // Le bras pivote sur son extrémité DROITE, pas sur son centre.
                .graphicsLayer {
                    rotationZ = angles.arm
                    transformOrigin = TransformOrigin(1f, 0.5f)
                },
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val h = this.size.height
                // tube
                drawRect(
                    Brush.verticalGradient(listOf(VinylArmMetal, VinylArmMetalDim)),
                    topLeft = Offset(h * 1.9f, h * 0.36f),
                    size = Size(this.size.width - h * 2.3f, h * 0.28f),
                )
                // tête de lecture + pointe accent
                drawRect(
                    Brush.linearGradient(listOf(VinylArmMetal, VinylArmMetalDim)),
                    topLeft = Offset.Zero,
                    size = Size(h * 2.1f, h),
                )
                drawRect(VinylPurple, topLeft = Offset(h * 0.35f, h), size = Size(h * 0.21f, h * 0.57f))
                // contrepoids
                drawRect(
                    Brush.linearGradient(listOf(VinylArmMetalDim, VinylArmShadow)),
                    topLeft = Offset(this.size.width - h * 1.6f, h * 0.07f),
                    size = Size(h * 1.6f, h * 0.86f),
                )
            }
        }
    }
}

/** Les deux angles du cycle, appariés : ce qui permet de les produire depuis une expression `if`. */
@Immutable
private data class LoaderAngles(
    val disc: Float,
    val arm: Float,
)

/**
 * Compose la transition infinie du cycle. N'est appelée que si le loader est animé : son groupe de
 * composition — et donc la transition — est détruit dès que ce n'est plus le cas.
 */
@Composable
private fun rememberLoaderAngles(): LoaderAngles {
    val t = rememberInfiniteTransition(label = "loader")

    val discAngle by t.animateFloat(
        initialValue = DISC_REST_ANGLE,
        targetValue = 720f,
        animationSpec =
            infiniteRepeatable(
                keyframes {
                    durationMillis = CYCLE
                    0f at 0 using LinearEasing
                    0f at 720 using CubicBezierEasing(0.5f, 0f, 0.9f, 0.45f) // démarrage
                    160f at 1140 using LinearEasing // plein régime
                    520f at 1680 using LinearEasing
                    650f at 2040 using LinearEasing // paliers de plus
                    700f at 2340 using LinearEasing // en plus serrés
                    716f at 2640 using LinearEasing
                    720f at 2880
                },
            ),
        label = "disc",
    )

    // Repos = pointe tangente au bord ; lecture = premier sillon.
    val armAngle by t.animateFloat(
        initialValue = ARM_REST_ANGLE,
        targetValue = ARM_REST_ANGLE,
        animationSpec =
            infiniteRepeatable(
                keyframes {
                    durationMillis = CYCLE
                    16f at 0
                    16f at 240 using CubicBezierEasing(0.34f, 0.06f, 0.2f, 1f)
                    1f at 750
                    1f at 2340 using CubicBezierEasing(0.34f, 0.06f, 0.2f, 1f)
                    16f at 2850
                },
            ),
        label = "arm",
    )

    return LoaderAngles(disc = discAngle, arm = armAngle)
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0A10)
@Composable
private fun VinylLoaderPreview() {
    AndroidAppTheme {
        VinylLoader()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0A10)
@Composable
private fun VinylLoaderAtRestPreview() {
    AndroidAppTheme {
        VinylLoader(animated = false)
    }
}
