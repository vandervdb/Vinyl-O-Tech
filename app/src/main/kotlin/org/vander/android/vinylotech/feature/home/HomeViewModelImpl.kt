package org.vander.android.vinylotech.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.vander.spotifyclient.domain.repository.LibraryRepository
import org.vander.spotifyclient.domain.usecase.PlaylistUseCase
import javax.inject.Inject

@HiltViewModel
class HomeViewModelImpl
    @Inject
    constructor(
        private val spotifyLibraryRepository: LibraryRepository,
        private val playlistUseCase: PlaylistUseCase,
    ) : ViewModel(),
        HomeViewModel {
        init {
            viewModelScope.launch {
                playlistUseCase.getAndUpdatePlaylistsFlow()
            }
        }
    }
