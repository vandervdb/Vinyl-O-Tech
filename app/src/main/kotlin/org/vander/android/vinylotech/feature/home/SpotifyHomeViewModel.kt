package org.vander.android.vinylotech.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.vander.core.domain.data.SpotifyUri
import org.vander.core.domain.player.PlayerCommand
import org.vander.core.domain.player.PlayerController
import org.vander.core.domain.playlist.PlaylistRepository
import org.vander.core.domain.recent.RecentlyPlayedRepository
import org.vander.core.domain.state.PlaybackState
import org.vander.core.logger.Logger
import javax.inject.Inject

/**
 * The Accueil screen's ViewModel: the user's playlists, and which one is playing.
 *
 * The playing playlist is read from the singleton [PlayerController]. Before the controller was
 * scoped, this ViewModel held a second, never-started instance whose state stayed empty — which
 * is why the grid was given `null` until now.
 */
@HiltViewModel
class SpotifyHomeViewModel
    @Inject
    constructor(
        private val playlistRepository: PlaylistRepository,
        val recentlyPlayedRepository: RecentlyPlayedRepository,
        private val controller: PlayerController,
        private val logger: Logger,
    ) : ViewModel(),
        HomeViewModel {
        override val state: StateFlow<HomeUiState> =
            combine(
                playlistRepository.playlists,
                controller.state,
                recentlyPlayedRepository.recentlyPlayed,
            ) { playlists, playback, _ ->
                HomeUiState(
                    playlists = playlists,
                    resume = playback.toResume(),
                    playingPlaylistId = playback.context.playlistId,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomeUiState())

        init {
            controller.start()
            viewModelScope.launch {
                playlistRepository
                    .refresh()
                    .onFailure { logger.e(TAG, "Error refreshing playlists", it) }
                recentlyPlayedRepository
                    .refresh()
                    .onFailure { logger.e(TAG, "Error refreshing recently played", it) }
            }
        }

        override fun playPlaylist(playlistId: String) {
            logger.d(TAG, "playPlaylist: $playlistId")
            logger.d(TAG, "ResumeListening: ${state.value.resume}")
            viewModelScope.launch { controller.dispatch(PlayerCommand.Play(SpotifyUri.playlist(playlistId))) }
        }

        private companion object {
            const val TAG = "SpotifyHomeViewModel"

            const val STOP_TIMEOUT_MS = 5_000L
        }

        private fun PlaybackState.toResume(): ResumeListening? {
            val title = context.title.ifEmpty { return null }
            return ResumeListening(
                title = title,
                subtitle = player.base.artistName,
                isSaved = null,
            )
        }
    }
