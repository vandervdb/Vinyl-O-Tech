package org.vander.core.domain.data

/**
 * What is playing right now plus what comes next, from the Web API's queue endpoint.
 *
 * @property currentlyPlaying `null` when nothing is playing.
 */
data class CurrentlyPlaying(
    val currentlyPlaying: Track? = null,
    val queue: Queue,
) {
    companion object {
        fun empty() = CurrentlyPlaying(null, Queue(emptyList()))
    }
}
