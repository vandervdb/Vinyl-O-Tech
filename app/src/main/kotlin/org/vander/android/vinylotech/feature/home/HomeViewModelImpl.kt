package org.vander.android.vinylotech.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.vander.core.domain.data.SpotifyUri
import org.vander.core.domain.player.PlayerCommand
import org.vander.core.logger.Logger
import org.vander.spotifyclient.domain.player.PlayerController
import org.vander.spotifyclient.domain.usecase.PlaylistUseCase
import javax.inject.Inject

@HiltViewModel
class HomeViewModelImpl
    @Inject
    constructor(
        private val playlistUseCase: PlaylistUseCase,
        private val controller: PlayerController,
        private val logger: Logger,
    ) : ViewModel(),
        HomeViewModel {

            val tag = "HomeViewModelImpl"
        override val playlists = playlistUseCase.playlists

        init {
            viewModelScope.launch {
                playlistUseCase.getAndUpdatePlaylistsFlow()
            }
        }

        override fun playPlaylist(playlistId: String) {
            logger.d(tag, "playPlaylist: $playlistId")
            viewModelScope.launch {
                controller.dispatch(PlayerCommand.Play(SpotifyUri.playlist(playlistId)))
            }
        }
    }
