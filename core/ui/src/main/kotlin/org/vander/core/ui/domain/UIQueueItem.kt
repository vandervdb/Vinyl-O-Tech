package org.vander.core.ui.domain

/**
 * One row of the queue.
 *
 * @property trackId bare Spotify id, without the `spotify:track:` prefix — usable as a
 *   `LazyColumn` key and as a path segment in a Web API call.
 */
data class UIQueueItem(
    val trackName: String,
    val artistName: String,
    val trackId: String,
) {
    companion object {
        fun empty(): UIQueueItem =
            UIQueueItem(
                trackName = "",
                artistName = "",
                trackId = "",
            )
    }
}
