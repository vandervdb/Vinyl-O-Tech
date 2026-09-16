package org.vander.fake.spotify

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.vander.core.domain.data.PlaybackContext
import org.vander.core.domain.player.PlayerCommand
import org.vander.core.domain.state.DomainPlayerState
import org.vander.core.domain.state.PlayerStateData
import org.vander.core.domain.state.SessionState
import org.vander.core.ui.domain.UIQueueItem
import org.vander.core.ui.presentation.viewmodel.PlayerViewModel
import org.vander.core.ui.state.PlayerUiState
import org.vander.core.ui.state.UIQueueState

/**
 * [PlayerViewModel] for `@Preview` and design work — no Hilt graph, no App Remote, no network.
 *
 * Every command moves the state the way the real player would, so an interactive preview
 * reacts: play/pause flips, a skip walks the queue, the heart toggles. [received] records what
 * was sent, for a test that only cares about the intent.
 *
 * @param initial starting state; defaults to a playing track at the head of a short queue.
 */
class FakePlayerViewModel(
    initial: PlayerUiState = sampleState(),
) : PlayerViewModel {
    private val _state = MutableStateFlow(initial)
    override val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    val received = mutableListOf<PlayerCommand>()

    override fun onCommand(command: PlayerCommand) {
        received += command
        _state.update { reduce(it, command) }
    }

    companion object {
        fun sampleState(): PlayerUiState {
            val queue =
                listOf(
                    UIQueueItem(trackName = "Nuits blanches", artistName = "Elia Faure", trackId = "t1"),
                    UIQueueItem(trackName = "Marée haute", artistName = "Nord Nord", trackId = "t2"),
                    UIQueueItem(trackName = "Braise", artistName = "Cléo Wend", trackId = "t3"),
                )
            val current = queue.first()
            return PlayerUiState(
                session = SessionState.Ready,
                player =
                    DomainPlayerState(
                        base =
                            PlayerStateData.empty().copy(
                                trackName = current.trackName,
                                artistName = current.artistName,
                                albumName = "Sillons",
                                trackId = current.trackId,
                                isPaused = false,
                                paused = false,
                                playing = true,
                                positionMs = 42_000,
                                durationMs = 214_000,
                            ),
                        isTrackSaved = false,
                    ),
                queue = UIQueueState(queue),
            )
        }
    }
}

/** What the real player would do, reduced to what a preview can show. */
internal fun reduce(
    state: PlayerUiState,
    command: PlayerCommand,
): PlayerUiState =
    when (command) {
        PlayerCommand.TogglePlayPause -> state.withPaused(!state.player.base.isPaused)
        PlayerCommand.Pause -> state.withPaused(true)
        PlayerCommand.Resume -> state.withPaused(false)
        PlayerCommand.SkipNext -> state.movedBy(1)
        PlayerCommand.SkipPrevious -> state.movedBy(-1)
        is PlayerCommand.SeekTo -> {
            val base = state.player.base
            state.withBase(base.copy(positionMs = command.positionMs.coerceIn(0, base.durationMs)))
        }
        is PlayerCommand.Play -> state.withPaused(false).copy(context = PlaybackContext(uri = command.uri))
        PlayerCommand.ToggleSave ->
            state.copy(player = state.player.copy(isTrackSaved = state.player.isTrackSaved != true))
    }

private fun PlayerUiState.withBase(base: PlayerStateData) = copy(player = player.copy(base = base))

private fun PlayerUiState.withPaused(paused: Boolean) =
    withBase(player.base.copy(isPaused = paused, paused = paused, playing = !paused))

/** Walks the queue; stays put at either end rather than wrapping around. */
private fun PlayerUiState.movedBy(delta: Int): PlayerUiState {
    val items = queue.items
    val target = items.getOrNull(items.indexOfFirst { it.trackId == player.base.trackId } + delta) ?: return this
    return copy(
        player =
            DomainPlayerState(
                base =
                    player.base.copy(
                        trackId = target.trackId,
                        trackName = target.trackName,
                        artistName = target.artistName,
                        positionMs = 0,
                    ),
                isTrackSaved = false,
            ),
    )
}
