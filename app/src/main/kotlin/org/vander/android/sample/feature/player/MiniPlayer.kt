package org.vander.android.sample.feature.player

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import org.vander.android.sample.R
import org.vander.android.sample.component.SpotifyTrackCover
import org.vander.android.sample.designsystem.component.MarqueeTextInfinite
import org.vander.core.domain.state.SessionState
import org.vander.core.logger.Logger
import org.vander.core.ui.domain.UIQueueItem
import org.vander.core.ui.presentation.viewmodel.PlayerViewModel

// Spotify's own "restart vs. go to previous track" cutoff for skipPrevious() — not
// documented by the SDK, approximated from observed behavior. Tune if it misfires.
private const val SKIP_PREVIOUS_RESTART_THRESHOLD_MS = 3000L

data class TrackParams(
    val tracksQueue: List<UIQueueItem>,
    val trackId: String = "",
    val isSaved: Boolean = false,
    val isPaused: Boolean,
    val positionMS: Long,
    val durationMS: Long,
)

@Suppress("FunctionNaming")
@Composable
fun MiniPlayer(
    viewModel: PlayerViewModel,
    logger: Logger,
) {
    val playerState by viewModel.domainPlayerState.collectAsStateWithLifecycle()
    val uIQueueState by viewModel.uiQueueState.collectAsStateWithLifecycle()

    logger.d("MiniPlayer", "Session is ready")
    logger.d("MiniPlayer", "Player state: $playerState")
    logger.d("MiniPlayer", "Queue state: $uIQueueState")

    if (uIQueueState.items.isNotEmpty()) {
        MiniPlayerContent(
            trackParams =
                TrackParams(
                    tracksQueue = uIQueueState.items,
                    trackId = playerState.base.trackId,
                    isSaved = playerState.isTrackSaved == true,
                    isPaused = playerState.base.isPaused,
                    positionMS = playerState.base.positionMs,
                    durationMS = playerState.base.durationMs,
                ),
            saveTrack = {
                logger.d("MiniPlayer", "Saving track: $it")
                viewModel.toggleSaveTrack(it)
            },
            skipNext = {
                logger.d("MiniPlayer", "Skipping next track")
                viewModel.skipNext()
            },
            skipPrevious = {
                logger.d("MiniPlayer", "Skipping previous track")
                viewModel.skipPrevious()
            },
            onPlayPause = { viewModel.togglePlayPause() },
            onSeekTo = { targetMs -> viewModel.seekTo(targetMs) },
            cover = {
                SpotifyTrackCover(
                    imageUri = playerState.base.coverId,
                    modifier = Modifier.size(48.dp),
                )
            },
            logger = logger,
        )
    }
}

@Composable
private fun MiniPlayerContent(
    trackParams: TrackParams,
    onPlayPause: () -> Unit,
    saveTrack: (String) -> Unit,
    skipNext: () -> Unit,
    skipPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    cover: @Composable () -> Unit,
    logger: Logger,
) {
    val pagerState = rememberPagerState(pageCount = { trackParams.tracksQueue.size })
    val currentTrackId = trackParams.trackId
    val currentTrackIndex = trackParams.tracksQueue.indexOfFirst { it.trackId == currentTrackId }

    // Workaround to prevent the swipe gesture callback (playTrack(newTrackId)) to be triggered
    var suppressSwipeCallback by remember { mutableStateOf(false) }

    LaunchedEffect(currentTrackId) {
        if (currentTrackIndex >= 0 && currentTrackIndex != pagerState.currentPage) {
            suppressSwipeCallback = true
            pagerState.animateScrollToPage(currentTrackIndex)
            delay(300)
            suppressSwipeCallback = false
        }
    }
    LaunchedEffect(pagerState.currentPage) {
        // currentTrackIndex == -1 means domainPlayerState hasn't caught up with a freshly
        // rebuilt uiQueueState yet — the real current track isn't identified in the queue,
        // so page 0 can't be trusted as a user swipe target.
        if (!suppressSwipeCallback && currentTrackIndex >= 0) {
            val newTrackId = trackParams.tracksQueue.getOrNull(pagerState.currentPage)?.trackId
            if (newTrackId != null && newTrackId != currentTrackId) {
                logger.d("MiniPlayer", "Swiped to trackId=$newTrackId")
                val newTrackIndex = trackParams.tracksQueue.indexOfFirst { it.trackId == newTrackId }
                if (currentTrackIndex > newTrackIndex) {
                    // Spotify's skipPrevious() only moves to the previous track when called
                    // close to the start of the current one; past that, it restarts the
                    // current track instead (confirmed via SpotifyPlayerClient logs: call
                    // accepted, track unchanged, position reset to 0). So: if we're already
                    // near the start, one call is enough; otherwise the first call just
                    // restarts and a second one (now near position 0) is needed to actually
                    // move back.
                    skipPrevious()
                    if (trackParams.positionMS > SKIP_PREVIOUS_RESTART_THRESHOLD_MS) {
                        skipPrevious()
                    }
                } else {
                    skipNext()
                }
            }
        }
    }

    Surface(
        tonalElevation = 4.dp,
        color = Color.Transparent,
        modifier =
            Modifier
                .padding(0.dp, 0.dp, 0.dp, 1.dp)
                .fillMaxWidth(),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier =
                    Modifier
                        .padding(0.dp)
                        .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                cover()

                Spacer(modifier = Modifier.padding(horizontal = 8.dp))

                Box(modifier = Modifier.weight(3f)) {
                    TracksQueue(pagerState, trackParams.tracksQueue)
                }

                IconButton(onClick = { saveTrack(trackParams.trackId) }) {
                    Icon(
                        imageVector = if (trackParams.isSaved) Icons.Default.CheckCircle else Icons.Default.AddCircle,
                        contentDescription =
                            if (trackParams.isSaved) {
                                stringResource(R.string.content_desc_remove_from_library)
                            } else {
                                stringResource(R.string.content_desc_save_to_library)
                            },
                    )
                }

                IconButton(onClick = onPlayPause) {
                    Icon(
                        imageVector = if (trackParams.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription =
                            if (trackParams.isPaused) {
                                stringResource(R.string.content_desc_play)
                            } else {
                                stringResource(R.string.content_desc_pause)
                            },
                    )
                }
            }

            val progress =
                run {
                    val dur = trackParams.durationMS
                    val pos = trackParams.positionMS
                    if (dur > 0L) (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f) else 0f
                }
            var barWidthPx by remember { mutableFloatStateOf(0F) }

            LinearProgressIndicator(
                progress = { progress },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .onGloballyPositioned { coords ->
                            barWidthPx = coords.size.width.toFloat()
                        }.pointerInput(trackParams.durationMS) {
                            detectTapGestures { offset ->
                                val dur = trackParams.durationMS
                                if (dur <= 0L || barWidthPx <= 0f) return@detectTapGestures
                                val fraction = (offset.x / barWidthPx).coerceIn(0f, 1f)
                                val targetMs = (dur * fraction).toLong()
                                onSeekTo(targetMs)
                            }
                        },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

@Composable
private fun TracksQueue(
    pagerState: PagerState,
    tracksQueue: List<UIQueueItem>,
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth(),
        flingBehavior = PagerDefaults.flingBehavior(state = pagerState),
        pageSpacing = 8.dp,
    ) { index ->
        val item = tracksQueue[index]
        TrackItem(
            trackName = item.trackName,
            artistName = item.artistName,
        )
    }
}

@Composable
private fun TrackItem(
    trackName: String,
    artistName: String,
) {
    Column(
        modifier = Modifier.padding(0.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        MarqueeTextInfinite(
            text = trackName,
            modifier = Modifier.width(120.dp),
        )
        Text(
            text = artistName,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
        )
    }
}

@Suppress("FunctionNaming")
@Composable
fun MiniPlayerWithPainter(
    viewModel: PlayerViewModel,
    coverPainter: Painter,
    logger: Logger,
) {
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    val playerState by viewModel.domainPlayerState.collectAsStateWithLifecycle()
    val uIQueueState by viewModel.uiQueueState.collectAsStateWithLifecycle()

    if (sessionState is SessionState.Ready) {
        MiniPlayerContent(
            trackParams =
                TrackParams(
                    tracksQueue = uIQueueState.items,
                    trackId = playerState.base.trackId,
                    isSaved = playerState.isTrackSaved == true,
                    isPaused = playerState.base.isPaused,
                    positionMS = playerState.base.positionMs,
                    durationMS = playerState.base.durationMs,
                ),
            saveTrack = {
                logger.d("MiniPlayer", "Saving track: $it")
                viewModel.toggleSaveTrack(it)
            },
            skipNext = {
                logger.d("MiniPlayer", "Skipping next track")
                viewModel.skipNext()
            },
            skipPrevious = {
                logger.d("MiniPlayer", "Skipping previous track")
                viewModel.skipPrevious()
            },
            onPlayPause = { viewModel.togglePlayPause() },
            onSeekTo = { targetMs -> viewModel.seekTo(targetMs) },
            cover = {
                SpotifyTrackCover(
                    painter = coverPainter,
                    modifier = Modifier.size(48.dp),
                )
            },
            logger = logger,
        )
    }
}

@Preview(showBackground = true)
@Suppress("FunctionNaming")
@Composable
fun MiniPlayerPreview() {
    PreviewMiniPlayerWithLocalCover()
}
