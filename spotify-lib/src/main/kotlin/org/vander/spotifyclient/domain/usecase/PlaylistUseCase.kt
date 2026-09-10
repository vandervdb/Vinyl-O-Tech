package org.vander.spotifyclient.domain.usecase

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.vander.core.domain.data.PlaylistCollection
import org.vander.core.logger.Logger
import org.vander.spotifyclient.domain.repository.SpotifyPlaylistRepository
import javax.inject.Inject

class PlaylistUseCase
    @Inject
    constructor(
        val playlistRepository: SpotifyPlaylistRepository,
        private val logger: Logger,
    ) {
        companion object Companion {
            private const val TAG = "PlaylistUseCase"
        }

        private val _playlists = MutableStateFlow(PlaylistCollection.empty())
        val playlists: StateFlow<PlaylistCollection> = _playlists.asStateFlow()

        suspend fun getAndUpdatePlaylistsFlow() {
            playlistRepository.getUserPlaylists().fold(
                onSuccess = { playlistCollection ->
                    logger.d(TAG, "Received user playlists: $playlistCollection")
                    _playlists.update { playlistCollection }
                },
                onFailure = {
                    _playlists.update { PlaylistCollection.empty() }
                    logger.e(TAG, "Error getting user playlists", it)
                },
            )
        }
    }
