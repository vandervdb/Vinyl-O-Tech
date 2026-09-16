package org.vander.android.vinylotech.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.vander.core.domain.data.SpotifyUri
import org.vander.core.domain.player.PlayerCommand
import org.vander.core.logger.Logger
import org.vander.spotifyclient.domain.player.PlayerController
import org.vander.spotifyclient.domain.usecase.PlaylistUseCase
import javax.inject.Inject

/**
 * The Accueil screen's ViewModel: the user's playlists, and which one is playing.
 *
 * The playing playlist is read from the singleton [PlayerController]. Before the controller was
 * scoped, this ViewModel held a second, never-started instance whose state stayed empty — which
 * is why the grid was given `null` until now.
 */
@HiltViewModel
class HomeViewModelImpl
    @Inject
    constructor(
        private val playlistUseCase: PlaylistUseCase,
        private val controller: PlayerController,
        private val logger: Logger,
    ) : ViewModel(),
        HomeViewModel {
        override val state: StateFlow<HomeUiState> =
            combine(
                playlistUseCase.playlists,
                controller.state.map { it.context.playlistId }.distinctUntilChanged(),
                ::HomeUiState,
            ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomeUiState())

        init {
            controller.start()
            viewModelScope.launch { playlistUseCase.getAndUpdatePlaylistsFlow() }
        }

        override fun playPlaylist(playlistId: String) {
            logger.d(TAG, "playPlaylist: $playlistId")
            viewModelScope.launch { controller.dispatch(PlayerCommand.Play(SpotifyUri.playlist(playlistId))) }
        }

        private companion object {
            const val TAG = "HomeViewModelImpl"

            const val STOP_TIMEOUT_MS = 5_000L
        }
    }
