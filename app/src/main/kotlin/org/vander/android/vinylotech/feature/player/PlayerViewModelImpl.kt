package org.vander.android.vinylotech.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.vander.core.domain.player.PlayerCommand
import org.vander.core.domain.state.PlaybackState
import org.vander.core.domain.state.SessionState
import org.vander.core.ui.domain.UIQueueItem
import org.vander.core.ui.presentation.viewmodel.PlayerViewModel
import org.vander.core.ui.state.PlayerUiState
import org.vander.core.ui.state.UIQueueState
import org.vander.spotifyclient.domain.data.session.SpotifySessionManager
import org.vander.spotifyclient.domain.player.PlayerController
import javax.inject.Inject

/**
 * The app's [PlayerViewModel]: it adapts [PlayerController] to the contract the Composables
 * consume, and holds no player logic of its own.
 *
 * Its only work is shaping: the session and the controller's state become one [PlayerUiState],
 * and the queue becomes `UIQueueItem`s here, on the app side, since `spotify-lib` no longer
 * depends on `core:ui`.
 *
 * It does not shut the session down when cleared: the controller is a singleton other screens
 * read, and the session's teardown belongs to `AppRoot`, on the Activity's `onStop`.
 */
@HiltViewModel
open class PlayerViewModelImpl
    @Inject
    constructor(
        private val controller: PlayerController,
        sessionManager: SpotifySessionManager,
    ) : ViewModel(),
        PlayerViewModel {
        override val state: StateFlow<PlayerUiState> =
            combine(sessionManager.sessionState, controller.state, ::toUiState)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                    initialValue = toUiState(sessionManager.sessionState.value, controller.state.value),
                )

        init {
            // Idempotent: harmless if another screen already started it.
            controller.start()
        }

        override fun onCommand(command: PlayerCommand) {
            viewModelScope.launch { controller.dispatch(command) }
        }

        private companion object {
            /** Survives a rotation, during which the UI unsubscribes and subscribes again. */
            const val STOP_TIMEOUT_MS = 5_000L

            fun toUiState(
                session: SessionState,
                playback: PlaybackState,
            ) = PlayerUiState(
                session = session,
                player = playback.player,
                queue = UIQueueState(playback.queue.map { UIQueueItem(it.name, it.artistName, it.id) }),
                context = playback.context,
            )
        }
    }
