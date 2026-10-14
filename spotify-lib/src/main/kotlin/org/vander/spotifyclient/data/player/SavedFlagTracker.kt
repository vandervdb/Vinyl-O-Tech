package org.vander.spotifyclient.data.player

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.vander.core.domain.library.LibraryRepository
import org.vander.core.domain.player.PlayerStateRepository
import org.vander.core.logger.Logger
import javax.inject.Inject

/**
 * Whether [trackId] is in the user's library. Carries its track so a reader can tell a flag
 * that belongs to the playing track from one left over by the previous track.
 */
internal data class SavedFlag(
    val trackId: String,
    val isSaved: Boolean,
)

/**
 * Tracks whether the playing track is saved, through the Web API library.
 *
 * The flag is looked up when the track changes, not on every tick, and again when the App
 * Remote signals a save from another device. An answer that arrives after the track changed
 * is dropped, so a slow lookup cannot overwrite the flag of the track that replaced it.
 *
 * Owned by [SpotifyPlayerController], which runs it in its own scope and only applies [flag]
 * while its track is the one playing.
 */
internal class SavedFlagTracker
    @Inject
    constructor(
        private val libraryRepository: LibraryRepository,
        private val playerStateRepository: PlayerStateRepository,
        private val logger: Logger,
    ) {
        private val _flag = MutableStateFlow<SavedFlag?>(null)
        val flag: StateFlow<SavedFlag?> = _flag.asStateFlow()

        /** Looks the flag up on every track change and remote save; suspends until cancelled. */
        suspend fun run() =
            coroutineScope {
                launch { lookUpOnTrackChange() }
                launch { lookUpOnSaveElsewhere() }
            }

        /**
         * Persists the change, then publishes it. The flag only moves on success, so a refused
         * call leaves the heart as it was rather than showing a state the library does not have.
         */
        suspend fun toggle(): Result<Unit> {
            val trackId = playerStateRepository.playerStateData.value.trackId
            if (trackId.isEmpty()) return Result.failure(IllegalStateException("ToggleSave: no track loaded"))

            val wasSaved = _flag.value?.takeIf { it.trackId == trackId }?.isSaved == true
            val result = if (wasSaved) libraryRepository.removeTrack(trackId) else libraryRepository.saveTrack(trackId)
            return result.onSuccess { publish(trackId, !wasSaved) }
        }

        private suspend fun lookUpOnTrackChange() {
            playerStateRepository.playerStateData
                .map { it.trackId }
                .filter { it.isNotEmpty() }
                .distinctUntilChanged()
                .collect { trackId -> lookUp(trackId) }
        }

        private suspend fun lookUpOnSaveElsewhere() {
            playerStateRepository.savedRemotelyChangedState
                .filter { it.isSaved && it.trackId.isNotEmpty() }
                .collect { event -> lookUp(event.trackId) }
        }

        private suspend fun lookUp(trackId: String) {
            libraryRepository
                .isTrackSaved(trackId)
                .onSuccess { publish(trackId, it) }
                .onFailure { logger.e(TAG, "Saved flag lookup failed for $trackId", it) }
        }

        /** Ignores an answer that arrives after the track changed. */
        private fun publish(
            trackId: String,
            isSaved: Boolean,
        ) {
            if (playerStateRepository.playerStateData.value.trackId != trackId) return
            _flag.value = SavedFlag(trackId, isSaved)
        }

        private companion object {
            const val TAG = "SavedFlagTracker"
        }
    }
