package org.vander.core.ui.domain

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
