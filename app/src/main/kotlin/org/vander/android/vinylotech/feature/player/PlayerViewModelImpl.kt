package org.vander.android.vinylotech.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.vander.core.domain.data.PlaybackContext
import org.vander.core.domain.data.SpotifyUri
import org.vander.core.domain.player.PlayerCommand
import org.vander.core.domain.state.DomainPlayerState
import org.vander.core.ui.domain.UIQueueItem
import org.vander.core.ui.presentation.viewmodel.PlayerViewModel
import org.vander.core.ui.state.UIQueueState
import org.vander.spotifyclient.domain.data.session.SpotifySessionManager
import org.vander.spotifyclient.domain.player.PlayerController
import javax.inject.Inject

/**
 * The app's [PlayerViewModel]: it adapts [PlayerController] to the contract the Composables
 * consume, and holds no player logic of its own.
 *
 * The queue is mapped to `UIQueueItem` here, on the app side, now that `spotify-lib` no longer
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
        override val sessionState = sessionManager.sessionState

        override val domainPlayerState: StateFlow<DomainPlayerState> =
            controller.state
                .map { it.player }
                .stateIn(viewModelScope, SharingStarted.Eagerly, controller.state.value.player)

        override val uiQueueState: StateFlow<UIQueueState> =
            controller.state
                .map { state -> UIQueueState(state.queue.map { UIQueueItem(it.name, it.artistName, it.id) }) }
                .stateIn(viewModelScope, SharingStarted.Eagerly, UIQueueState.empty())

        override val playbackContext: StateFlow<PlaybackContext> =
            controller.state
                .map { it.context }
                .stateIn(viewModelScope, SharingStarted.Eagerly, controller.state.value.context)

        init {
            // Idempotent: harmless if another screen already started it.
            controller.start()
        }

        override fun togglePlayPause() = send(PlayerCommand.TogglePlayPause)

        override fun skipNext() = send(PlayerCommand.SkipNext)

        override fun skipPrevious() = send(PlayerCommand.SkipPrevious)

        override fun playTrack(trackId: String) = send(PlayerCommand.Play(SpotifyUri.track(trackId)))

        override fun toggleSave() = send(PlayerCommand.ToggleSave)

        override fun seekTo(position: Long) = send(PlayerCommand.SeekTo(position))

        private fun send(command: PlayerCommand) {
            viewModelScope.launch { controller.dispatch(command) }
        }
    }
