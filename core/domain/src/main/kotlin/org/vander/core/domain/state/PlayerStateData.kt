package org.vander.core.domain.state

/**
 * Flat snapshot of the App Remote player, decoupled from the Spotify SDK types.
 *
 * Produced by `PlayerState.toPlayerStateData` in `spotify-lib`; read that mapper before
 * relying on a field, several are not what their name suggests:
 * - [trackId] and [coverId] are bare ids, prefixes already stripped (`spotify:track:`,
 *   `ImageId{spotify:image:`), so they concatenate straight into a Web API URL.
 * - [isPaused] and [paused] carry the same value, [playing] is its negation.
 * - [stopped] and [seeking] are always `false` — the SDK exposes no equivalent.
 * - [skippingNext] and [skippingPrevious] are filled from `canSkipNext == false` /
 *   `canSkipPrev == false`, i.e. they mean "skip is forbidden", not "a skip is running".
 *
 * @property positionMs playback head, in milliseconds.
 * @property durationMs track length in milliseconds; `0` when no track is loaded.
 */
data class PlayerStateData(
    val trackName: String,
    val artistName: String,
    val albumName: String,
    val coverId: String,
    val trackId: String,
    val isPaused: Boolean,
    val playing: Boolean,
    val paused: Boolean,
    val stopped: Boolean,
    val shuffling: Boolean,
    val repeating: Boolean,
    val seeking: Boolean,
    val skippingNext: Boolean,
    val skippingPrevious: Boolean,
    val positionMs: Long,
    val durationMs: Long,
) {
    companion object {
        fun empty(): PlayerStateData =
            PlayerStateData(
                trackName = "",
                artistName = "",
                albumName = "",
                coverId = "",
                trackId = "",
                isPaused = true,
                playing = false,
                paused = true,
                stopped = true,
                shuffling = false,
                repeating = false,
                seeking = false,
                skippingNext = false,
                skippingPrevious = false,
                positionMs = 0,
                durationMs = 0,
            )
    }
}
