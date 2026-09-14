package org.vander.android.vinylotech.feature.connection

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
import org.vander.android.vinylotech.R
import org.vander.android.vinylotech.designsystem.AndroidAppTheme
import org.vander.android.vinylotech.designsystem.VinylPurple
import org.vander.android.vinylotech.designsystem.VinylTextEmphasis
import org.vander.android.vinylotech.designsystem.VinylTextMuted
import org.vander.android.vinylotech.designsystem.VinylTextTertiary
import org.vander.android.vinylotech.designsystem.VotDimens
import org.vander.android.vinylotech.designsystem.component.VinylLoader
import org.vander.android.vinylotech.designsystem.loginHalo
import org.vander.android.vinylotech.designsystem.modifier.drawGrainOverlay
import org.vander.core.domain.state.SessionState

/**
 * Screen 01 "Connexion Spotify" from the Vinyl O'Tech design — the app's start
 * destination ([org.vander.android.vinylotech.navigation.ConnectionRoute]).
 *
 * Takes an intent lambda, not the SpotifySessionManager: the manager needs the
 * activity result launcher and the Activity, both of which live in AppRoot. A
 * screen holding it could not be previewed either.
 */
@Composable
fun ConnectionScreen(
    state: SessionState,
    onContinueWithSpotify: () -> Unit,
    spinning: Boolean,
) {
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
    AndroidAppTheme { ConnectionScreen(state, onContinueWithSpotify = {}, true) }
}
