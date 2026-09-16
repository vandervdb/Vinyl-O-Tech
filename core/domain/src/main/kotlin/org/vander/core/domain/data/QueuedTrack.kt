package org.vander.core.domain.data

/**
 * One entry of the playback queue, reduced to what a queue display needs.
 *
 * Exists so the player's published state carries a plain value instead of the Web API's
 * `Track`, which is a heavy `class` without structural equality — and so `spotify-lib` no
 * longer builds `core:ui`'s `UIQueueItem` itself.
 *
 * @property id bare Spotify id; empty for a slot the Web API returned as `null`, which is
 *   kept rather than dropped so positions stay stable.
 * @property artistName first artist only, empty when the track has none.
 */
data class QueuedTrack(
    val id: String,
    val name: String,
    val artistName: String,
)
