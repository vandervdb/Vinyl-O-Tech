package org.vander.android.vinylotech.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.vander.android.vinylotech.designsystem.AndroidAppTheme
import org.vander.android.vinylotech.designsystem.VinylInk
import org.vander.android.vinylotech.designsystem.VinylPurple
import org.vander.android.vinylotech.designsystem.VinylPurpleSoft
import org.vander.android.vinylotech.designsystem.VinylSurfaceCard
import org.vander.android.vinylotech.designsystem.VotDimens

/** Un tour de disque, en ms. Maquette : `animation: spin 6s linear infinite`. */
private const val SPIN_CYCLE_MS = 6000

/** Durée de la décélération jusqu'à l'arrêt, quelle que soit la course restante. */
private const val SPIN_STOP_MS = 1400

/**
 * Course minimale laissée au disque pour ralentir. Sans elle, une pause déclenchée juste
 * avant la fin d'un tour ne laisserait que quelques degrés, et l'arrêt paraîtrait net.
 */
private const val SPIN_STOP_MIN_SWEEP = 180f

/** Course du bras entre repos et lecture, en ms. */
private const val ARM_TRAVEL_MS = 600

/**
 * Cotes relevées sur la carte MiniPlayer de la maquette, exprimées en fraction du diamètre
 * du disque (40 px) pour que le composant reste redimensionnable — même vocabulaire que
 * [VinylLoader], qui écrit ses cotes en `size * 0.073f`.
 */
private const val HOLE_RATIO = 0.225f // 9/40

private const val ARM_WIDTH_RATIO = 0.05f // 2/40

private const val ARM_LENGTH_RATIO = 0.6f // 24/40

private const val ARM_RADIUS_RATIO = 0.05f // 2/40

private const val PIVOT_RATIO = 0.175f // 7/40

private const val FULL_TURN = 360f

/** `transform: rotate(36deg)` sur le bras en lecture, pivot en haut au centre. */
private const val ARM_PLAY_ANGLE = 36f

/** Bras relevé : angle nul, donc pendu à la verticale au bord du disque. */
private const val ARM_REST_ANGLE = 0f

/** Même courbe que le bras de [VinylLoader], pour que les deux platines bougent pareil. */
private val ARM_EASING = CubicBezierEasing(0.34f, 0.06f, 0.2f, 1f)

/**
 * Platine miniature de la carte MiniPlayer : pochette ronde qui tourne, trou central, bras
 * de lecture et son pivot par-dessus.
 *
 * Volontairement plus fruste que [VinylLoader] : à cette taille, ses sillons, son
 * contrepoids et sa cellule ne seraient qu'une bouillie de pixels. Elle lui emprunte en
 * revanche sa grammaire de mouvement : le disque ralentit jusqu'à revenir à son angle de
 * départ au lieu de se figer net, et le bras se relève pendant cette décélération.
 *
 * La maquette dessine le disque en dégradé conique ; on lui substitue la vraie pochette, d'où
 * le slot [cover] plutôt qu'une couleur. Le slot ne reçoit aucun type domaine, ce qui garde le
 * composant dans le design system.
 *
 * @param size diamètre du disque. Le bras et le pivot débordent légèrement en haut à droite.
 * @param spinning pilote les deux animations. Le passage à `false` ne coupe rien net : le
 *   disque termine sa course en décélérant jusqu'à 0°, le bras remonte au repos, et les
 *   deux repartent de leur position courante si la lecture reprend entre-temps.
 * @param cover la pochette, dessinée en `fillMaxSize()` puis rognée en cercle par ce composant.
 */
@Composable
fun VinylMiniTurntable(
    modifier: Modifier = Modifier,
    size: Dp = VotDimens.miniPlayerDisc,
    spinning: Boolean = true,
    cover: @Composable () -> Unit,
) {
    val discAngle = remember { Animatable(0f) }
    val armAngle = remember { Animatable(ARM_REST_ANGLE) }

    LaunchedEffect(spinning) {
        if (spinning) {
            launch { armAngle.animateTo(ARM_PLAY_ANGLE, tween(ARM_TRAVEL_MS, easing = ARM_EASING)) }
            // Un tour à la fois, en repartant de l'angle courant : à la pause, l'effet est
            // annulé et l'Animatable conserve sa valeur, donc l'arrêt enchaîne sans saut.
            while (isActive) {
                discAngle.animateTo(
                    targetValue = discAngle.value + FULL_TURN,
                    animationSpec = tween(SPIN_CYCLE_MS, easing = LinearEasing),
                )
                discAngle.snapTo(discAngle.value % FULL_TURN)
            }
        } else {
            launch { armAngle.animateTo(ARM_REST_ANGLE, tween(ARM_TRAVEL_MS, easing = ARM_EASING)) }
            if (discAngle.value == 0f) return@LaunchedEffect
            // On termine toujours sur 0°, quitte à ajouter un tour pour avoir de quoi
            // ralentir. LinearOutSlowIn part à pleine vitesse et s'éteint : c'est l'inertie
            // d'un plateau, pas une transition qui démarre.
            val remaining = FULL_TURN - discAngle.value
            val sweep = if (remaining < SPIN_STOP_MIN_SWEEP) remaining + FULL_TURN else remaining
            discAngle.animateTo(
                targetValue = discAngle.value + sweep,
                animationSpec = tween(SPIN_STOP_MS, easing = LinearOutSlowInEasing),
            )
            discAngle.snapTo(0f)
        }
    }

    Box(modifier.size(size)) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = discAngle.value
                    clip = true
                    shape = CircleShape
                },
        ) {
            cover()
        }

        Box(
            Modifier
                .align(Alignment.Center)
                .size(size * HOLE_RATIO)
                .clip(CircleShape)
                .background(VinylInk),
        )

        // `top: 1px; right: -1px` de la maquette : le bras dépasse d'un pixel sur la droite.
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = size * 0.025f, y = size * 0.025f)
                .width(size * ARM_WIDTH_RATIO)
                .height(size * ARM_LENGTH_RATIO)
                .graphicsLayer {
                    rotationZ = armAngle.value
                    transformOrigin = TransformOrigin(0.5f, 0f)
                }.clip(RoundedCornerShape(size * ARM_RADIUS_RATIO))
                .background(VinylPurpleSoft),
        )

        // Le pivot est bordé de la couleur de la carte, pas d'une teinte à lui : c'est ce
        // liseré qui le détache du bras sur lequel il est posé.
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = size * 0.1f, y = -size * 0.05f)
                .size(size * PIVOT_RATIO)
                .clip(CircleShape)
                .background(VinylPurpleSoft)
                .border(1.dp, VinylSurfaceCard, CircleShape),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF221C2E)
@Composable
private fun VinylMiniTurntablePreview() {
    AndroidAppTheme {
        VinylMiniTurntable {
            Box(Modifier.fillMaxSize().background(VinylPurple))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF221C2E)
@Composable
private fun VinylMiniTurntablePausedPreview() {
    AndroidAppTheme {
        VinylMiniTurntable(spinning = false) {
            Box(Modifier.fillMaxSize().background(VinylPurple))
        }
    }
}
