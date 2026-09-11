package org.vander.android.sample.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.vander.android.sample.designsystem.AndroidAppTheme
import org.vander.android.sample.designsystem.Typography
import org.vander.android.sample.designsystem.VinylFeedbackError
import org.vander.android.sample.designsystem.VinylFeedbackErrorText
import org.vander.android.sample.designsystem.VinylFeedbackNeutralText
import org.vander.android.sample.designsystem.VinylFeedbackSuccess
import org.vander.android.sample.designsystem.VinylFeedbackSuccessText
import org.vander.android.sample.designsystem.VinylOutlineStrong
import org.vander.android.sample.designsystem.VinylPurple
import org.vander.android.sample.designsystem.VinylSnackSurface
import org.vander.android.sample.designsystem.VinylTextEmphasis
import org.vander.android.sample.designsystem.VinylTextPrimary
import org.vander.android.sample.designsystem.VinylTextSecondary
import org.vander.android.sample.designsystem.VotDimens

/*
 * Snackbar HAUTE — descend sous la status bar, jamais par le bas.
 * Le bas de l'écran appartient au dock du mini-player et à la barre d'onglets :
 * une snackbar Material par défaut les recouvrirait.
 *
 * Ne pas utiliser androidx SnackbarHost : il est ancré en bas et son Snackbar
 * n'expose ni le ton, ni le filet de minuteur. VinylSnackbarHost le remplace.
 */

enum class VinylSnackTone { Error, Success, Info, Offline }

data class VinylSnackbarData(
    val message: String,
    val title: String? = null,
    val tone: VinylSnackTone = VinylSnackTone.Info,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
    /** Persistante : ni minuteur, ni auto-dismiss (mode hors ligne). */
    val persistent: Boolean = false,
    val icon: ImageVector? = null,
)

private data class ToneColors(
    val accent: Color,
    val onAccent: Color,
    val border: Color,
)

private fun colorsFor(tone: VinylSnackTone) =
    when (tone) {
        VinylSnackTone.Error ->
            ToneColors(VinylFeedbackError, VinylFeedbackErrorText, VinylFeedbackError.copy(alpha = 0.40f))
        VinylSnackTone.Success ->
            ToneColors(VinylFeedbackSuccess, VinylFeedbackSuccessText, VinylFeedbackSuccess.copy(alpha = 0.34f))
        VinylSnackTone.Info -> ToneColors(VinylPurple, VinylFeedbackNeutralText, VinylPurple.copy(alpha = 0.42f))
        VinylSnackTone.Offline -> ToneColors(Color.Transparent, VinylFeedbackNeutralText, VinylOutlineStrong)
    }

/**
 * État hôte. Une seule snackbar à l'écran : [show] remplace celle en cours
 * (pas de file d'attente — un message périmé ne vaut pas la peine d'attendre).
 */
class VinylSnackbarHostState {
    var current: VinylSnackbarData? by mutableStateOf(null)
        private set

    /** Change à chaque show : relance le minuteur même si le contenu est identique. */
    var serial: Int by mutableStateOf(0)
        private set

    fun show(data: VinylSnackbarData) {
        current = data
        serial += 1
    }

    fun dismiss() {
        current = null
    }
}

@Composable
fun rememberVinylSnackbarHostState() = remember { VinylSnackbarHostState() }

/**
 * À poser en DERNIER enfant du Box racine de l'écran (au-dessus du grain, sous
 * les dialogues). Gère l'auto-dismiss et l'animation d'entrée/sortie.
 */
@Composable
fun VinylSnackbarHost(
    state: VinylSnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val data = state.current
    val serial = state.serial

    LaunchedEffect(serial) {
        val d = state.current ?: return@LaunchedEffect
        if (d.persistent) return@LaunchedEffect
        delay(if (d.actionLabel != null) DURATION_WITH_ACTION else DURATION_DEFAULT)
        if (state.serial == serial) state.dismiss()
    }

    Box(modifier = modifier.fillMaxWidth().statusBarsPadding()) {
        AnimatedVisibility(
            visible = data != null,
            enter = slideInVertically(tween(220)) { -it / 3 } + fadeIn(tween(220)),
            exit = slideOutVertically(tween(160)) { -it / 3 } + fadeOut(tween(160)),
        ) {
            data?.let {
                VinylSnackbar(
                    data = it,
                    serial = serial,
                    onDismiss = state::dismiss,
                    modifier =
                        Modifier.padding(
                            horizontal = VotDimens.snackMarginH,
                            vertical = VotDimens.snackMarginTop,
                        ),
                )
            }
        }
    }
}

@Composable
fun VinylSnackbar(
    data: VinylSnackbarData,
    modifier: Modifier = Modifier,
    serial: Int = 0,
    onDismiss: () -> Unit = {},
) {
    val tone = colorsFor(data.tone)
    val shape = RoundedCornerShape(VotDimens.snackRadius)

    // Filet de minuteur : 1f → 0f sur la durée d'affichage.
    var started by remember(serial) { mutableStateOf(false) }
    LaunchedEffect(serial) { started = true }
    val total = if (data.actionLabel != null) DURATION_WITH_ACTION else DURATION_DEFAULT
    val progress by animateFloatAsState(
        targetValue = if (started) 0f else 1f,
        animationSpec = tween(durationMillis = total.toInt()),
        label = "snackTimer",
    )

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .shadow(VotDimens.snackElevation, shape, clip = false)
                .clip(shape)
                .background(VinylSnackSurface)
                .border(1.dp, tone.border, shape)
                .clickable(enabled = !data.persistent, onClick = onDismiss),
    ) {
        Row(
            modifier =
                Modifier.padding(
                    start = VotDimens.space14,
                    end = VotDimens.space14,
                    top = VotDimens.space14,
                    bottom = VotDimens.space14,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(VotDimens.space12),
        ) {
            SnackGlyph(data, tone)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(VotDimens.space2),
            ) {
                data.title?.let {
                    // Maquette : 14sp SG SemiBold (titleSmall vaut 15sp).
                    Text(
                        it,
                        style = Typography.titleSmall.copy(fontSize = 14.sp, lineHeight = 18.sp),
                        color = VinylTextEmphasis,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    data.message,
                    style =
                        if (data.title == null) {
                            Typography.bodySmall.copy(fontSize = 14.sp, lineHeight = 20.sp)
                        } else {
                            Typography.bodySmall
                        },
                    color = if (data.title == null) VinylTextPrimary else VinylTextSecondary,
                    maxLines = if (data.title == null) 2 else 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            data.actionLabel?.let { label ->
                Text(
                    label,
                    // Maquette : 13sp SG SemiBold.
                    style = Typography.titleSmall.copy(fontSize = 13.sp),
                    color = tone.onAccent,
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(VotDimens.snackActionRadius))
                            .background(tone.accent.copy(alpha = 0.12f))
                            .clickable {
                                data.onAction?.invoke()
                                onDismiss()
                            }.padding(horizontal = 10.dp, vertical = 7.dp),
                )
            }
        }

        if (!data.persistent) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(VotDimens.snackTimer)
                    .background(Color.White.copy(alpha = 0.07f)),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(VotDimens.snackTimer)
                        .background(if (data.tone == VinylSnackTone.Offline) VinylTextSecondary else tone.accent),
                )
            }
        }
    }
}

@Composable
private fun SnackGlyph(
    data: VinylSnackbarData,
    tone: ToneColors,
) {
    val box = Modifier.size(VotDimens.snackGlyph)
    when {
        data.tone == VinylSnackTone.Info && data.icon == null ->
            Box(box.clip(CircleShape).border(1.dp, tone.border, CircleShape)) {
                VinylDisc(modifier = Modifier.size(VotDimens.snackGlyph))
            }
        else ->
            Box(
                modifier =
                    box
                        .clip(CircleShape)
                        .background(
                            if (data.tone == VinylSnackTone.Offline) {
                                Color.White.copy(alpha = 0.07f)
                            } else {
                                tone.accent.copy(alpha = 0.15f)
                            },
                        ).border(1.dp, tone.border, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                data.icon?.let {
                    Icon(it, contentDescription = null, tint = tone.onAccent, modifier = Modifier.size(14.dp))
                }
            }
    }
}

private const val DURATION_DEFAULT = 4_000L
private const val DURATION_WITH_ACTION = 6_000L

/*
 * Usage :
 *
 * val snack = rememberVinylSnackbarHostState()
 * Box(Modifier.fillMaxSize()) {
 *     LibraryScreen(onSyncFailed = {
 *         snack.show(VinylSnackbarData(
 *             title = "Synchronisation échouée",
 *             message = "14 albums n'ont pas été importés.",
 *             tone = VinylSnackTone.Error,
 *             actionLabel = "Réessayer",
 *             onAction = viewModel::retrySync,
 *             icon = Icons.Rounded.ErrorOutline,
 *         ))
 *     })
 *     GrainOverlay()
 *     VinylSnackbarHost(snack, Modifier.align(Alignment.TopCenter))
 * }
 *
 * Hors ligne : tone = Offline, persistent = true, pas d'action — la snackbar
 * reste jusqu'au retour du réseau, où l'on appelle snack.dismiss().
 */

@Preview(showBackground = true, backgroundColor = 0xFF0C0A10)
@Composable
private fun VinylSnackbarPreviews() {
    AndroidAppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            VinylSnackbar(
                data =
                    VinylSnackbarData(
                        title = "Info Snackbar",
                        message = "This is a default info snackbar.",
                        tone = VinylSnackTone.Info,
                        icon = Icons.Rounded.Info,
                    ),
            )

            VinylSnackbar(
                data =
                    VinylSnackbarData(
                        title = "Success Snackbar",
                        message = "Your action was successful!",
                        tone = VinylSnackTone.Success,
                        icon = Icons.Rounded.CheckCircle,
                    ),
            )

            VinylSnackbar(
                data =
                    VinylSnackbarData(
                        title = "Error Snackbar",
                        message = "Something went wrong. Please try again.",
                        tone = VinylSnackTone.Error,
                        icon = Icons.Rounded.ErrorOutline,
                        actionLabel = "Retry",
                    ),
            )

            VinylSnackbar(
                data =
                    VinylSnackbarData(
                        title = "Offline Snackbar",
                        message = "You are currently offline.",
                        tone = VinylSnackTone.Offline,
                        icon = Icons.Rounded.WifiOff,
                        persistent = true,
                    ),
            )
        }
    }
}
