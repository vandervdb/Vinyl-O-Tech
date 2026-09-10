package org.vander.core.domain.state

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
