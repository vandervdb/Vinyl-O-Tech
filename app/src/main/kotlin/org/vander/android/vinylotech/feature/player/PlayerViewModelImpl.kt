package org.vander.android.vinylotech.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.vander.core.domain.data.SpotifyUri
import org.vander.core.domain.state.DomainPlayerState
import org.vander.core.logger.Logger
import org.vander.core.ui.presentation.viewmodel.PlayerViewModel
import org.vander.spotifyclient.domain.data.session.SpotifySessionManager
import org.vander.spotifyclient.domain.repository.LibraryRepository
import org.vander.spotifyclient.domain.usecase.PlayerUseCase
import javax.inject.Inject

/**
 * The app's [PlayerViewModel]: it owns no player logic, it adapts [PlayerUseCase] to the
 * contract the Composables consume and marshals every call onto `viewModelScope`.
 *
 * It injects [LibraryRepository] alongside the use case because writing to the user's
 * library is not routed through [PlayerUseCase] yet — [PlayerUseCase.toggleSaveTrackState]
 * only flips the local flag. Two collaborators therefore share ownership of "this track is
 * saved", which is why [toggleSave] has to call both.
 */
@HiltViewModel
open class PlayerViewModelImpl
    @Inject
    constructor(
        private val playerUseCase: PlayerUseCase,
        private val spotifyLibraryRepository: LibraryRepository,
        sessionManager: SpotifySessionManager,
        private val logger: Logger,
    ) : ViewModel(),
        PlayerViewModel {
        override val domainPlayerState: StateFlow<DomainPlayerState> =
            playerUseCase.domainPlayerState

        override val sessionState = sessionManager.sessionState

        override val uiQueueState = playerUseCase.uIQueueState

        override val playbackContext = playerUseCase.playbackContext

        /**
         * Starts the collectors that feed [domainPlayerState] and [uiQueueState], once per instance.
         *
         * It lives in the initializer rather than behind a `start()` on [PlayerViewModel] because
         * the composition cannot express "once per ViewModel": a `LaunchedEffect` re-runs on every
         * rotation and on every return to the destination. [PlayerUseCase.init] launches three
         * collectors on infinite flows, never returns and is not idempotent, so a second call would
         * add three more rather than replace them — here the language guarantees there is no second
         * call.
         *
         * The job is cancelled with `viewModelScope`, i.e. when this ViewModel is cleared.
         */
        init {
            viewModelScope.launch { playerUseCase.init() }
        }

        /**
         * Adds the current track to the library, or removes it.
         *
         * The direction comes from [DomainPlayerState.isTrackSaved] and the target from
         * [DomainPlayerState.base], both read from a single snapshot — which is what keeps a caller
         * from pairing one track's flag with another track's id.
         *
         * Does nothing while no track is loaded.
         */
        override fun toggleSave() {
            val state = domainPlayerState.value
            val trackId = state.base.trackId
            if (trackId.isEmpty()) return

            logger.d(TAG, "toggleSave: isSaved=${state.isTrackSaved}, trackId=$trackId")
            val action = if (state.isTrackSaved == true) ::removeTrackFromSaved else ::saveTrack
            action(trackId)
        }

        override fun togglePlayPause() {
            viewModelScope.launch {
                playerUseCase.togglePlayPause()
            }
        }

        override fun skipNext() {
            viewModelScope.launch {
                playerUseCase.skipNext()
            }
        }

        override fun skipPrevious() {
            viewModelScope.launch {
                playerUseCase.skipPrevious()
            }
        }

        override fun playTrack(trackId: String) {
            viewModelScope.launch {
                playerUseCase.play(SpotifyUri.track(trackId))
            }
        }

        override fun seekTo(position: Long) {
            viewModelScope.launch {
                playerUseCase.seekTo(position)
            }
        }

        /**
         * Ends the Spotify session when the ViewModel is destroyed.
         *
         * The teardown is launched on `viewModelScope`, which `ViewModel.clear()` also cancels
         * during the same destruction — so whether [PlayerUseCase.shutDown] gets to run depends on
         * the order of those two steps. A scope owned outside the ViewModel would remove the doubt.
         */
        override fun onCleared() {
            super.onCleared()
            viewModelScope.launch { playerUseCase.shutDown() }
        }

        /**
         * Persists the track, then mirrors the change locally. The local flip is conditional on
         * success, so a failed call leaves the UI showing the previous state rather than lying.
         *
         * Private: it is one of the two branches [toggleSave] selects through a method reference.
         */
        private fun saveTrack(trackId: String) {
            viewModelScope.launch {
                spotifyLibraryRepository
                    .saveTrack(trackId)
                    .onSuccess {
                        playerUseCase.toggleSaveTrackState(trackId)
                    }.onFailure {
                        logger.e(TAG, "Error saving track", it)
                    }
            }
        }

        /** The removal branch of [toggleSave]; see [saveTrack] for the shape. */
        private fun removeTrackFromSaved(trackId: String) {
            viewModelScope.launch {
                spotifyLibraryRepository
                    .removeTrack(trackId)
                    .onSuccess {
                        playerUseCase.toggleSaveTrackState(trackId)
                    }.onFailure {
                        logger.e(TAG, "Error removing track", it)
                    }
            }
        }

        companion object Companion {
            private const val TAG = "PlayerViewModelImpl"
        }
    }
