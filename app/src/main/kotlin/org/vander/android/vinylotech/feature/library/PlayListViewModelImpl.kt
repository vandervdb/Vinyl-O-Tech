package org.vander.android.vinylotech.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.vander.core.domain.data.PlaylistCollection
import org.vander.core.ui.presentation.viewmodel.PlaylistViewModel
import org.vander.spotifyclient.domain.usecase.PlaylistUseCase
import javax.inject.Inject

/**
 * Exposes the use case's flow directly. [refresh] used to launch a new, never-ending collector
 * on each call — ten refreshes, ten collectors writing the same value — through `android.util.Log`
 * rather than the injected logger.
 */
@HiltViewModel
open class PlayListViewModelImpl
    @Inject
    constructor(
        private val useCase: PlaylistUseCase,
    ) : ViewModel(),
        PlaylistViewModel {
        override val playlists: StateFlow<PlaylistCollection> = useCase.playlists

        override fun refresh() {
            viewModelScope.launch { useCase.getAndUpdatePlaylistsFlow() }
        }
    }
