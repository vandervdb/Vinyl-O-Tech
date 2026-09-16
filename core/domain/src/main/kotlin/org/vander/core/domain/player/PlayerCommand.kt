package org.vander.core.domain.player

import org.vander.core.domain.data.SpotifyUri

/**
 * Every action the player accepts, as values.
 *
 * One `dispatch(command)` instead of a method per action: the controller keeps a narrow
 * surface, a ViewModel forwards user intents without growing a method each, and a test or a
 * fake can record exactly what was asked. The `when` over it is exhaustive, so adding a
 * command fails to compile wherever it is not handled.
 */
sealed interface PlayerCommand {
    data object TogglePlayPause : PlayerCommand

    data object Pause : PlayerCommand

    data object Resume : PlayerCommand

    data object SkipNext : PlayerCommand

    data object SkipPrevious : PlayerCommand

    /** @property positionMs absolute playback head in milliseconds, not a delta. */
    data class SeekTo(
        val positionMs: Long,
    ) : PlayerCommand

    /** Plays whatever [uri] addresses — a track, a playlist, an album. */
    data class Play(
        val uri: SpotifyUri,
    ) : PlayerCommand

    /**
     * Adds the current track to the library, or removes it. The direction comes from the
     * published saved flag at the moment the command runs, not from the caller.
     */
    data object ToggleSave : PlayerCommand
}
