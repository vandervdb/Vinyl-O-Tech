package org.vander.android.sample.feature.connection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.vander.android.sample.R
import org.vander.android.sample.designsystem.AndroidAppTheme
import org.vander.android.sample.designsystem.VinylPurple
import org.vander.android.sample.designsystem.VinylTextEmphasis
import org.vander.android.sample.designsystem.VinylTextMuted
import org.vander.android.sample.designsystem.VinylTextTertiary
import org.vander.android.sample.designsystem.VotDimens
import org.vander.android.sample.designsystem.component.VINYL_LOADER_CYCLE_MS
import org.vander.android.sample.designsystem.component.VinylLoader
import org.vander.android.sample.designsystem.loginHalo
import org.vander.android.sample.designsystem.modifier.drawGrainOverlay
import org.vander.core.domain.state.SessionState
import kotlin.time.TimeSource

/**
 * Screen 01 "Connexion Spotify" from the Vinyl O'Tech design — the app's start
 * destination ([org.vander.android.sample.navigation.ConnectionRoute]).
 *
 * Takes an intent lambda, not the SpotifySessionManager: the manager needs the
 * activity result launcher and the Activity, both of which live in AppRoot. A
 * screen holding it could not be previewed either.
 */
@Composable
fun ConnectionScreen(
    state: SessionState,
    onContinueWithSpotify: () -> Unit,
) {
    val connecting = state is SessionState.Authorizing || state is SessionState.ConnectingRemote
    val spinning = rememberHeldForWholeCycles(connecting, VINYL_LOADER_CYCLE_MS)

    // Le libellé suit le disque, pas l'état brut : il dit « connexion » tant que ça tourne.
    val grooveText =
        when {
            spinning -> stringResource(R.string.loader_connecting)
            state is SessionState.Ready -> stringResource(R.string.loader_connected)
            else -> stringResource(R.string.loader_brand)
        }

    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .drawGrainOverlay(alpha = 0.82f),
    ) {
        val glowSize = maxWidth * (480f / 428f)
        val glowOffsetX = maxWidth * (-110f / 428f)
        val glowOffsetY = maxHeight * (-160f / 908f)

        val haloRadiusPx = with(LocalDensity.current) { glowSize.toPx() / 2f }
        Box(
            modifier =
                Modifier
                    .size(glowSize)
                    .offset(x = glowOffsetX, y = glowOffsetY)
                    .background(brush = loginHalo(haloRadiusPx)),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        start = VotDimens.screenPadding,
                        end = VotDimens.screenPadding,
                        bottom = VotDimens.screenBottom,
                    ),
        ) {
            BoxWithConstraints(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter,
            ) {
                VinylLoader(
                    size = min(maxWidth, maxHeight),
                    animated = spinning,
                    grooveText = grooveText,
                )
            }

            Text(
                text = stringResource(R.string.home_headline),
                style = MaterialTheme.typography.headlineLarge,
                color = VinylTextEmphasis,
            )

            Text(
                text = stringResource(R.string.home_subtitle),
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp,
                        lineHeight = 23.sp,
                        letterSpacing = 0.sp,
                    ),
                color = VinylTextMuted,
                modifier =
                    Modifier.padding(
                        top = VotDimens.space14,
                        bottom = VotDimens.space32,
                    ),
            )

            Button(
                enabled = !spinning,
                onClick = onContinueWithSpotify,
                shape = RoundedCornerShape(percent = 50),
                contentPadding = PaddingValues(vertical = VotDimens.buttonPadding),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = VotDimens.buttonElevation,
                            shape = RoundedCornerShape(percent = 50),
                            ambientColor = VinylPurple,
                            spotColor = VinylPurple,
                        ),
            ) {
                Text(stringResource(R.string.home_continue_button))
            }

            Text(
                text = stringResource(R.string.home_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = VinylTextTertiary,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = VotDimens.space20),
            )
        }
    }
}

/**
 * Suit [active], mais ne retombe à `false` qu'à la fin du cycle d'animation en cours :
 * la durée totale est arrondie au multiple supérieur de [cycleMs], avec un cycle entier
 * au minimum. Deux garanties en découlent.
 *
 * 1. Une cause qui disparaît plus vite que l'animation — une autorisation déjà accordée,
 *    servie en quelques centaines de ms — laisse quand même voir un tour complet.
 * 2. La coupure tombe toujours sur la fin d'un cycle, donc sur la position de repos du
 *    loader (disque à 0°, bras relevé) : aucun saut visuel, quelle que soit la durée.
 *
 * Si [active] repasse à `true` pendant l'attente, l'effet est annulé puis relancé : le
 * compteur repart de zéro, ce qui est le comportement voulu pour une seconde tentative.
 */
@Composable
private fun rememberHeldForWholeCycles(
    active: Boolean,
    cycleMs: Int,
): Boolean {
    var held by remember { mutableStateOf(active) }
    var startedAt by remember { mutableStateOf(TimeSource.Monotonic.markNow()) }

    LaunchedEffect(active) {
        if (active) {
            startedAt = TimeSource.Monotonic.markNow()
            held = true
            return@LaunchedEffect
        }
        if (!held) return@LaunchedEffect
        val elapsed = startedAt.elapsedNow().inWholeMilliseconds
        // Division entière + 1 : arrondi au-dessus, et jamais moins d'un cycle.
        val whole = (elapsed / cycleMs + 1) * cycleMs
        delay(whole - elapsed)
        held = false
    }

    return held
}

private class SessionStates : PreviewParameterProvider<SessionState> {
    override val values =
        sequenceOf(
            SessionState.Idle,
            SessionState.Authorizing,
            SessionState.ConnectingRemote,
            SessionState.Failed(IllegalStateException("Token expiré")),
        )
}

@Preview
@Composable
private fun PreviewConnection(
    @PreviewParameter(SessionStates::class) state: SessionState,
) {
    AndroidAppTheme { ConnectionScreen(state, onContinueWithSpotify = {}) }
}
