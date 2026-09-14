package org.vander.android.vinylotech.feature.library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.vander.core.domain.data.PlaylistCollection
import org.vander.core.ui.presentation.viewmodel.PlaylistViewModel
import org.vander.spotifyclient.domain.usecase.PlaylistUseCase
import javax.inject.Inject

@HiltViewModel
open class PlayListViewModelImpl
    @Inject
    constructor(
        private val useCase: PlaylistUseCase,
    ) : ViewModel(),
        PlaylistViewModel {
        private val _playlists = MutableStateFlow(PlaylistCollection.empty())
        override val playlists: StateFlow<PlaylistCollection> = _playlists.asStateFlow()

        override fun refresh() {
            viewModelScope.launch {
                useCase.getAndUpdatePlaylistsFlow()
                collectPlayListState()
            }
        }

        private suspend fun collectPlayListState() {
            Log.d("PlayListViewModelImpl", "Collecting playlists state...")
            useCase.playlists.collect { playlistState ->
                Log.d("PlayListViewModelImpl", "Received playlists state: $playlistState")
                _playlists.value = playlistState
            }
        }
    }
