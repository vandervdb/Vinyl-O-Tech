package org.vander.android.sample.feature.library

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.vander.android.sample.R
import org.vander.android.sample.designsystem.AndroidAppTheme
import org.vander.android.sample.designsystem.SpotifyGreen
import org.vander.android.sample.feature.player.MiniPlayer
import org.vander.core.domain.data.User
import org.vander.core.domain.state.SessionState
import org.vander.core.logger.Logger
import org.vander.core.logger.test.FakeLogger
import org.vander.core.ui.presentation.viewmodel.PlayerViewModel
import org.vander.core.ui.presentation.viewmodel.PlaylistViewModel
import org.vander.core.ui.presentation.viewmodel.UserViewModel
import org.vander.fake.spotify.FakePlayerViewModel
import org.vander.fake.spotify.FakePlaylistViewModel
import org.vander.fake.spotify.FakeUserViewModel

@Suppress("FunctionNaming")
@Composable
fun SpotifyScreen(
    playerViewModel: PlayerViewModel,
    playlistViewModel: PlaylistViewModel,
    userViewModel: UserViewModel,
    launchStartup: Boolean = true,
    logger: Logger,
) {
    val tag = "SpotifyScreen"
    val sessionState by playerViewModel.sessionState.collectAsStateWithLifecycle()
    val user by userViewModel.currentUser.collectAsStateWithLifecycle(User.empty)

    if (launchStartup) {
        val activity = LocalActivity.current
        LaunchedEffect(key1 = activity) {
            playerViewModel.startUp()
            playlistViewModel.refresh()
        }
    }

    LaunchedEffect(user) {
        userViewModel.refresh()
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 10.dp),
    ) {
        logger.d(tag, "Session state: $sessionState")
        when (sessionState) {
            is SessionState.Idle -> {
                logger.d(tag, "Idle...")
            }

            is SessionState.Authorizing -> {
                logger.d(tag, "Authorizing...")
            }

            is SessionState.ConnectingRemote -> {
                logger.d(tag, "Connecting to remote...")
                CircularProgressIndicator()
            }

            is SessionState.Ready -> {
                Text(
                    text = stringResource(R.string.spotify_playlists_title),
                    textAlign = TextAlign.Center,
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                PlaylistComponent(playlistViewModel, Modifier.weight(1f))
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                MiniPlayer(playerViewModel, logger)
            }

            is SessionState.Failed -> {
                Text(
                    text =
                        stringResource(
                            R.string.spotify_error_prefix,
                            (sessionState as SessionState.Failed).exception.message.orEmpty(),
                        ),
                    color = MaterialTheme.colorScheme.error,
                )
            }

            SessionState.IsPaused -> TODO()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("FunctionNaming")
@Composable
fun SpotifyTopBar(displayName: String?) {
    CenterAlignedTopAppBar(
        title = {
            Column {
                Image(
                    painter = painterResource(id = R.drawable.spotify_full_logo_black),
                    contentDescription = stringResource(R.string.content_desc_spotify_logo),
                    modifier =
                        Modifier
                            .height(32.dp),
                    contentScale = ContentScale.Fit,
                )
                Row(modifier = Modifier.padding(start = 32.dp)) {
                    Text(
                        text = stringResource(R.string.spotify_by_prefix),
                        style =
                            MaterialTheme.typography.labelSmall.copy(
                                fontStyle = FontStyle.Italic,
                                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            ),
                        color = Color.Black,
                    )
                    Text(
                        text =
                            displayName.takeUnless { it.isNullOrBlank() }
                                ?: stringResource(R.string.spotify_display_name_fallback),
                        style =
                            MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                            ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = SpotifyGreen,
                titleContentColor = Color.Black,
            ),
    )
}

@Preview(showBackground = true, name = "SpotifyScreen Preview")
@Suppress("FunctionNaming")
@Composable
fun SpotifyScreenPreview() {
    val fakePlayerViewModel = FakePlayerViewModel()
    val fakePlaylistViewModel = FakePlaylistViewModel()
    val fakeUserViewModel = FakeUserViewModel()
    val logger: Logger = FakeLogger()
    AndroidAppTheme {
        SpotifyScreen(
            playerViewModel = fakePlayerViewModel,
            playlistViewModel = fakePlaylistViewModel,
            userViewModel = fakeUserViewModel,
            launchStartup = false,
            logger,
        )
    }
}
