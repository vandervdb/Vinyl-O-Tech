package org.vander.core.domain.data

data class PlaylistCollection(
    val items: List<Playlist>,
) {
    companion object {
        fun empty() = PlaylistCollection(emptyList())
    }
}
