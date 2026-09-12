package org.vander.core.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response of `GET /me/player/queue`.
 *
 * @property currentlyPlaying absent when playback is stopped.
 * @property queue upcoming tracks. Entries are nullable, so the mapper substitutes
 *   `Track.empty()` for a null slot rather than dropping it.
 */
@Serializable
data class CurrentlyPlayingWithQueueDto(
    @SerialName("currently_playing") val currentlyPlaying: TrackDto? = null,
    val queue: List<TrackDto?> = emptyList(),
)
