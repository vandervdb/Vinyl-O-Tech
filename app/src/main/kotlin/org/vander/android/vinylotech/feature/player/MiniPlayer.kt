package org.vander.android.vinylotech.feature.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import org.vander.android.vinylotech.R
import org.vander.android.vinylotech.component.SpotifyTrackCover
import org.vander.android.vinylotech.designsystem.VinylOutlineStrong
import org.vander.android.vinylotech.designsystem.VinylPurple
import org.vander.android.vinylotech.designsystem.VinylSurfaceCard
import org.vander.android.vinylotech.designsystem.VinylTextPrimary
import org.vander.android.vinylotech.designsystem.VotDimens
import org.vander.android.vinylotech.designsystem.component.MarqueeTextInfinite
import org.vander.android.vinylotech.designsystem.component.VinylMiniTurntable
import org.vander.core.domain.player.PlayerCommand
import org.vander.core.domain.state.SessionState
import org.vander.core.logger.Logger
import org.vander.core.ui.domain.UIQueueItem
import org.vander.core.ui.presentation.viewmodel.PlayerViewModel
import org.vander.core.ui.state.PlayerUiState

// Spotify's own "restart vs. go to previous track" cutoff for skipPrevious() — not
// documented by the SDK, approximated from observed behavior. Tune if it misfires.
private const val SKIP_PREVIOUS_RESTART_THRESHOLD_MS = 3000L

// Cotes de la carte MiniPlayer relevées sur la maquette (écrans 02 · 03 · 04 · 05 · 08 · 10) :
// padding 9px 12px, gap 11px, bordure 1px rgba(124,92,255,0.3), barre de 2px sous le titre.
private val CARD_PADDING_H = 12.dp

private val CARD_PADDING_V = 9.dp

private val CARD_GAP = 11.dp

private val CARD_BORDER_WIDTH = 1.dp

private const val CARD_BORDER_ALPHA = 0.3f

private val TEXT_BAR_GAP = 3.dp

private val PROGRESS_HEIGHT = 2.dp

/** Le glyphe fait 15-16 px dans la maquette ; la cible tactile reste [VotDimens.touchMin]. */
private val CONTROL_ICON = 18.dp

/** Écart entre « aimer » et « lecture » : ils forment une paire, pas deux zones distinctes. */
private val CONTROL_GAP = 0.dp

/** Décale la lecture vers la droite sans réduire la marge de la carte. */
private val CONTROL_PAUSE_NUDGE = 0.dp

data class TrackParams(
    val tracksQueue: List<UIQueueItem>,
    val trackId: String = "",
    val isSaved: Boolean = false,
    val isPaused: Boolean,
    val positionMS: Long,
    val durationMS: Long,
)

@Composable
fun MiniPlayer(
    viewModel: PlayerViewModel,
    logger: Logger,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MiniPlayerContent(
        trackParams = state.toTrackParams(),
        onToggleSave = { viewModel.onCommand(PlayerCommand.ToggleSave) },
        skipNext = {
            logger.d("MiniPlayer", "Skipping next track")
            viewModel.onCommand(PlayerCommand.SkipNext)
        },
        skipPrevious = {
            logger.d("MiniPlayer", "Skipping previous track")
            viewModel.onCommand(PlayerCommand.SkipPrevious)
        },
        onPlayPause = { viewModel.onCommand(PlayerCommand.TogglePlayPause) },
        onSeekTo = { targetMs -> viewModel.onCommand(PlayerCommand.SeekTo(targetMs)) },
        cover = {
            SpotifyTrackCover(
                imageUri = state.player.base.coverId,
                modifier = Modifier.fillMaxSize(),
            )
        },
        logger = logger,
    )
}

private fun PlayerUiState.toTrackParams() =
    TrackParams(
        tracksQueue = queue.items,
        trackId = player.base.trackId,
        isSaved = player.isTrackSaved == true,
        isPaused = player.base.isPaused,
        positionMS = player.base.positionMs,
        durationMS = player.base.durationMs,
    )

@Composable
private fun MiniPlayerContent(
    trackParams: TrackParams,
    onPlayPause: () -> Unit,
    onToggleSave: () -> Unit,
    skipNext: () -> Unit,
    skipPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    cover: @Composable () -> Unit,
    logger: Logger,
) {
    val pagerState = rememberPagerState(pageCount = { trackParams.tracksQueue.size })
    val currentTrackId = trackParams.trackId
    val currentTrackIndex = trackParams.tracksQueue.indexOfFirst { it.trackId == currentTrackId }

    // Keeps a programmatic page change (the player moved on) from being read as a user swipe
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
        // currentTrackIndex == -1 means the player snapshot hasn't caught up with a freshly
        // rebuilt queue yet — the real current track isn't identified in the queue,
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
        shape = MaterialTheme.shapes.large,
        color = VinylSurfaceCard,
        border = BorderStroke(CARD_BORDER_WIDTH, VinylPurple.copy(alpha = CARD_BORDER_ALPHA)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = CARD_PADDING_H, vertical = CARD_PADDING_V),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CARD_GAP),
        ) {
            VinylMiniTurntable(spinning = !trackParams.isPaused, cover = cover)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(TEXT_BAR_GAP),
            ) {
                TracksQueue(pagerState, trackParams.tracksQueue)

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
                            .height(PROGRESS_HEIGHT)
                            .clip(RoundedCornerShape(PROGRESS_HEIGHT))
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
                    color = VinylPurple,
                    trackColor = VinylOutlineStrong,
                    // Material3 dessine par défaut un écart et une pastille de fin ; la maquette
                    // montre un trait plein.
                    gapSize = 0.dp,
                    drawStopIndicator = {},
                )
            }

            // Les deux contrôles forment leur propre groupe : l'espacement de la carte les
            // écartait autant que le disque et le texte, alors qu'ils vont ensemble.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CONTROL_GAP),
            ) {
                IconButton(
                    onClick = onToggleSave,
                    modifier = Modifier.size(VotDimens.touchMin),
                ) {
                    Icon(
                        imageVector = if (trackParams.isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription =
                            if (trackParams.isSaved) {
                                stringResource(R.string.content_desc_remove_from_library)
                            } else {
                                stringResource(R.string.content_desc_save_to_library)
                            },
                        tint = VinylPurple,
                        modifier = Modifier.size(CONTROL_ICON),
                    )
                }

                IconButton(
                    onClick = onPlayPause,
                    modifier =
                        Modifier
                            .size(VotDimens.touchMin)
                            .offset(x = CONTROL_PAUSE_NUDGE),
                ) {
                    Icon(
                        imageVector = if (trackParams.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription =
                            if (trackParams.isPaused) {
                                stringResource(R.string.content_desc_play)
                            } else {
                                stringResource(R.string.content_desc_pause)
                            },
                        tint = VinylTextPrimary,
                        modifier = Modifier.size(CONTROL_ICON),
                    )
                }
            }
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
    Column(verticalArrangement = Arrangement.Center) {
        // `fadeColor` doit valoir le fond réel, sinon les bords fondus du défilement
        // laissent apparaître la couleur du thème par-dessus la carte.
        MarqueeTextInfinite(
            text = trackName,
            modifier = Modifier.fillMaxWidth(),
            fadeColor = VinylSurfaceCard,
        )
        MarqueeTextInfinite(
            text = artistName,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodySmall.copy(color = VinylTextPrimary),
            fadeColor = VinylSurfaceCard,
        )
    }
}

@Composable
fun MiniPlayerWithPainter(
    viewModel: PlayerViewModel,
    coverPainter: Painter,
    logger: Logger,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.session !is SessionState.Ready) return

    MiniPlayerContent(
        trackParams = state.toTrackParams(),
        onToggleSave = { viewModel.onCommand(PlayerCommand.ToggleSave) },
        skipNext = {
            logger.d("MiniPlayer", "Skipping next track")
            viewModel.onCommand(PlayerCommand.SkipNext)
        },
        skipPrevious = {
            logger.d("MiniPlayer", "Skipping previous track")
            viewModel.onCommand(PlayerCommand.SkipPrevious)
        },
        onPlayPause = { viewModel.onCommand(PlayerCommand.TogglePlayPause) },
        onSeekTo = { targetMs -> viewModel.onCommand(PlayerCommand.SeekTo(targetMs)) },
        cover = {
            SpotifyTrackCover(
                painter = coverPainter,
                modifier = Modifier.fillMaxSize(),
            )
        },
        logger = logger,
    )
}

@Preview(showBackground = true)
@Composable
fun MiniPlayerPreview() {
    PreviewMiniPlayerWithLocalCover()
}
