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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import org.vander.android.sample.R
import org.vander.android.sample.designsystem.AndroidAppTheme
import org.vander.android.sample.designsystem.VinylPurple
import org.vander.android.sample.designsystem.VinylTextEmphasis
import org.vander.android.sample.designsystem.VinylTextMuted
import org.vander.android.sample.designsystem.VinylTextTertiary
import org.vander.android.sample.designsystem.VotDimens
import org.vander.android.sample.designsystem.component.VinylLogoMark
import org.vander.android.sample.designsystem.loginHalo
import org.vander.android.sample.designsystem.modifier.drawGrainOverlay

/**
 * Screen 01 "Connexion Spotify" from the Vinyl O'Tech design — the app's start
 * destination ([org.vander.android.sample.navigation.ConnectionRoute]).
 *
 * Takes an intent lambda, not the SpotifySessionManager: the manager needs the
 * activity result launcher and the Activity, both of which live in AppRoot. A
 * screen holding it could not be previewed either.
 */
@Suppress("FunctionNaming")
@Composable
fun ConnectionScreen(onContinueWithSpotify: () -> Unit) {
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
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.CenterStart,
            ) {
                VinylLogoMark()
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
                modifier = Modifier.fillMaxWidth().padding(top = VotDimens.space20),
            )
        }
    }
}

@Preview(showBackground = true, name = "ConnectionScreen Preview")
@Suppress("FunctionNaming")
@Composable
fun ConnectionScreenPreview() {
    AndroidAppTheme {
        ConnectionScreen(onContinueWithSpotify = {})
    }
}
