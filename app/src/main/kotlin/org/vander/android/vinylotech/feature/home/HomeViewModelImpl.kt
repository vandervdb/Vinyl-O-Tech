package org.vander.android.vinylotech.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.vander.core.domain.data.SpotifyUri
import org.vander.core.logger.Logger
import org.vander.spotifyclient.domain.repository.LibraryRepository
import org.vander.spotifyclient.domain.usecase.PlayerUseCase
import org.vander.spotifyclient.domain.usecase.PlaylistUseCase
import javax.inject.Inject

@HiltViewModel
class HomeViewModelImpl
    @Inject
    constructor(
        private val spotifyLibraryRepository: LibraryRepository,
        private val playlistUseCase: PlaylistUseCase,
        private val playerUseCase: PlayerUseCase,
        private val logger: Logger,
    ) : ViewModel(),
        HomeViewModel {
        override val playlists = playlistUseCase.playlists

        init {
            viewModelScope.launch {
                playlistUseCase.getAndUpdatePlaylistsFlow()
            }
        }

        override fun playPlaylist(playlistId: String) {
            logger.d("HomeViewModelImpl", "playPlaylist: $playlistId")
            viewModelScope.launch {
                playerUseCase.play(SpotifyUri.playlist(playlistId))
            }
        }
    }
