package org.vander.core.domain.data

data class CurrentlyPlaying(
    val currentlyPlaying: Track? = null,
    val queue: Queue,
) {
    companion object {
        fun empty() = CurrentlyPlaying(null, Queue(emptyList()))
    }
}
