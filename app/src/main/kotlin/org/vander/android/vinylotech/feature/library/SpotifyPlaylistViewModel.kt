package org.vander.android.vinylotech.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.vander.core.domain.data.PlaylistCollection
import org.vander.core.domain.playlist.PlaylistRepository
import org.vander.core.ui.presentation.viewmodel.PlaylistViewModel
import javax.inject.Inject

/**
 * Exposes the use case's flow directly.
 */
@HiltViewModel
open class SpotifyPlaylistViewModel
    @Inject
    constructor(
        private val repository: PlaylistRepository,
    ) : ViewModel(),
        PlaylistViewModel {
        override val playlists: StateFlow<PlaylistCollection> = repository.playlists

        override fun refresh() {
            viewModelScope.launch { repository.refresh().onFailure { } }
        }
    }
